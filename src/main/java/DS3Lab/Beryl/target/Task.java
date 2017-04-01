package DS3Lab.Beryl.target;

import DS3Lab.Beryl.frontend.Query;
import DS3Lab.Beryl.ir.*;
import DS3Lab.Beryl.type.*;
import DS3Lab.Beryl.frontend.Utils;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.*;

class Task {

    int batch_size = 100;
    int begin, end, batch_num;

    String target;
    ArrayList<Set<String>> args = new ArrayList<>();
    ArrayList<TaskType> method = new ArrayList<>();

    HashMap<String, Integer> optdim = new HashMap<>();

    Task(Query query) {
        this.target = query.target();
        for (int i = 0; i < query.optLevel(); ++i) {
            method.add(query.type(i));
            HashSet<String> s = new HashSet<>();
            s.addAll(query.args(i));
            args.add(s);
        }
        sgdOptimize();
    }

    private Value optValue(Value val, Const var, Integer begin, Integer end, HashMap<String, String> varMap) {
        switch (val.type()) {
            case OP: {
                Value[] args = new Value[val.args().length];
                for (int i = 0; i < args.length; ++i) {
                    args[i] = optValue(val.arg(i), var, begin, end, varMap);
                    if (args[i] == null) {
                        return null;
                    }
                }
                return new Value(val.op(), args);
            }
            case OP_WITH_VAR: {
                Value arg = optValue(val.arg(0), var, begin, end, varMap);
                int ib = val.range().begin().minVal();
                int ie = val.range().end().maxVal();
                Value bg = val.range().begin(), ed = val.range().end();
                if (bg.relate(var) || ed.relate(var)) {
                    Value zero = new Value(arg.shape(), arg.dtype());
                    Value cond = null;
                    if (bg.relate(var)) {
                        bg = optValue(bg, var, begin, end, varMap);
                        cond = new Value("ge", new Value[]{new Value(val.var(), new Value[0]), bg});
                        bg = new Value(ib);
                    }
                    if (ed.relate(var)) {
                        ed = optValue(ed, var, begin, end, varMap);
                        Value c = new Value("le", new Value[]{new Value(val.var(), new Value[0]), ed});
                        cond = (cond == null ? c : new Value("and", new Value[]{cond, c}));
                        ed = new Value(ie);
                    }
                    arg = new Value("select", new Value[]{cond, arg, zero});
                }
                Range v = new Range(bg, ed);
                return new Value(val.var(), v, val.op(), arg);
            }
            case REF: {
                Entry e = val.ref();
                if (e.name().equals(var.name())) {
                    return null;
                }
                if (varMap.containsKey(e.name())) {
                    return new Value(Context.getConst(varMap.get(e.name())), new Value[0]);
                }
                int vi = -1;
                Value[] idx = val.index();
                for (int i = 0; i < val.index().length; ++i) {
                    if (idx[i].type() == ValueType.REF && idx[i].ref().name().equals(var.name())) {
                        vi = i;
                        break;
                    }
                }
                if (vi != -1) {
                    Entry n = trySGDOptimize(val.ref(), vi, begin, end);
                    if (n == null) {
                        return null;
                    }
                    e = n;
                    Value[] nidx = new Value[idx.length - 1];
                    for (int i = 0, j = 0; i < idx.length; ++i) {
                        if (i != vi) {
                            nidx[j++] = optValue(idx[i], var, begin, end, varMap);
                        }
                    }
                    idx = nidx;
                }

                ArrayList<Integer> rels = new ArrayList<>();
                for (int i = 0; i < idx.length; ++i) {
                    if (idx[i].relate(var)) {
                        rels.add(i);
                    }
                }
                return solveExtract(e, idx, var, begin, end, rels, varMap);
            }
        }
        return val;
    }

    private Value solveExtract(Entry e, Value[] idx, Const var, Integer begin, Integer end, ArrayList<Integer> rels, HashMap<String, String> varMap) {
        if (rels.size() == 0) {
            for (int i = 0; i < idx.length; ++i) {
                idx[i] = optValue(idx[i], var, begin, end, varMap);
            }
            return new Value(e, idx);
        }
        int rel = rels.remove(0);

        Const lp = Context.getConst(Utils.tmpStr());
        Value[] index = idx.clone();
        index[rel] = new Value(lp, new Value[0]);
        Value sub = solveExtract(e, index, var, begin, end, rels, varMap);
        Value ind = optValue(idx[rel], var, begin, end, varMap);
        if (ind == null || sub == null) {
            return null;
        }
        Value concat = new Value(lp, new Range(new Value(idx[rel].minVal()), new Value(idx[rel].maxVal())), "concat", sub);
        return new Value("extract", new Value[]{concat, new Value("sub", new Value[]{ind, new Value(idx[rel].minVal())})});
    }

    private Entry trySGDOptimize(Entry tar, int vari, int begin, int end) {
        String newN = "B_" + tar.name();
        if (optdim.containsKey(tar.name()) && optdim.get(tar.name()) != vari) {
            return null;
        }
        if (Context.getType(newN) == SymbolType.VIEW) {
            return Context.getView(newN);
        }
        optdim.put(tar.name(), vari);
        View newV = new View(newN, tar, vari, batch_size);
        if (tar.type() != EntryType.VIEW) {
            return newV;
        }
        View view = tar.asView();
        view.calcRanges();

        HashMap<String, String> varMap = new HashMap<>();
        for (int i = 0, j = 0; i < view.indexNum(); ++i) {
            if (i != vari) {
                varMap.put(view.idx(i), newV.idx(j));
                j++;
            }
        }
        Range[] viewR = new Range[view.indexNum() - 1];
        for (int i = 0, j = 0; i < view.indexNum(); ++i) {
            if (i != vari) {
                viewR[j] = new Range(new Value(view.min(i)), new Value(view.max(i)));
                j++;
            }
        }
        Assign[] assigns = view.getAssigns();

        Value def = null;

        boolean trival = true;

        Const var = view.index(vari);

        for (Assign ass : assigns) {
            Range rng = ass.range(vari);
            Integer b = rng.getBegin(), e = rng.getEnd();
            if (b == null || begin != b || e == null || end != e) {
                return null;
            }

            boolean relate = false;

            Range[] range = new Range[ass.indexNum() - 1];
            for (int i = 0, j = 0; i < view.indexNum(); ++i) {
                if (i != vari) {
                    Value bg = optValue(ass.range(i).begin(), view.index(vari), begin, end, varMap);
                    Value ed = optValue(ass.range(i).end(), view.index(vari), begin, end, varMap);
                    if (bg == null || ed == null) {
                        return null;
                    }
                    range[j++] = new Range(bg, ed);
                }
            }

            Value cond = null;
            for (int i = 0, j = 0; i < view.indexNum(); ++i) {
                if (i != vari) {
                    if (ass.range(i).begin().relate(var)) {
                        relate = true;
                        trival = false;
                        Value cond_i = new Value("ge", new Value[]{new Value(newV.index(j), new Value[0]), range[j].begin()});
                        if (cond == null) {
                            cond = cond_i;
                        }
                        else {
                            cond = new Value("and", new Value[]{cond, cond_i});
                        }
                    }
                    if (ass.range(i).end().relate(var)) {
                        relate = true;
                        trival = false;
                        Value cond_i = new Value("le", new Value[]{new Value(newV.index(j), new Value[0]), range[j].end()});
                        if (cond == null) {
                            cond = cond_i;
                        }
                        else {
                            cond = new Value("and", new Value[]{cond, cond_i});
                        }
                    }
                    j++;
                }
            }

            Value val = optValue(ass.value(), view.index(vari), begin, end, varMap);

            if (relate) {
                if (def == null) {
                    def = val;
                }
                else {
                    def = new Value("select", new Value[]{cond, val, def});
                }
            }
            else {
                newV.putAssign(new Assign(range, val));
            }
        }

        if (!trival) {
            newV.putAssign(new Assign(viewR, def));
        }
        newV.calcRanges();
        return newV;
    }

    private boolean sgdOptimize() throws ParseCancellationException {
        View view = Context.getView(target);
        Assign[] assigns = view.getAssigns();
        if (assigns.length != 1) {
            return false;
        }
        Assign ass = assigns[0];
        if (ass.value().type() != ValueType.OP_WITH_VAR || !ass.value().op().equals("sum")) {
            return false;
        }
        Integer begin = ass.value().range().getBegin();
        Integer end = ass.value().range().getEnd();
        if (begin == null || end == null) {
            throw new ParseCancellationException("don't know sum's var's range");
        }
        Value opt = optValue(ass.value().arg(0), ass.value().var(), begin, end, new HashMap<>());
        if (opt == null) {
            return false;
        }
        View n = new View(Utils.tmpStr(), 0);
        n.putAssign(new Assign(new Range[0], new Value("Rsum", new Value[]{opt})));
        this.target = n.name();
        this.begin = begin;
        this.end = end;
        batch_num = (end - begin + 1) / batch_size;
        return true;
    }
}
