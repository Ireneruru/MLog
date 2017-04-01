grammar Beryl;

@parser::header {
import DS3Lab.Beryl.frontend.*;
import DS3Lab.Beryl.type.*;
import java.util.*;
}

/*
program : statement* ;

index : ID | INT ;

set_index : '{' expression (',' expression )*  '}'
             | index ;

var_desc : var_range ( ',' var_range )* ;

var_range : ID '=' expression '...' expression ;

statement : const_def | schema | assignment | feed | evaluation ;

const_def : ID ':=' ( INT | FLOAT ) ;

schema : tensor ( ',' tensor )* 'in' ID ( '^' set_index )? ( ',' var_desc )? ;

tensor : ID ( '_' ( ID | '{' ID ( ',' ID )* '}' ) )? ;

assignment : ID ( '_' set_index )? '=' expression ( ',' var_desc )? ;

expression : expression '+' expression_m
          | expression '-' expression_m
          | expression_m ;

expression_m : expression_m '*' expression_o
            | expression_m '.' expression_o
            | expression_m '/' expression_o
            | expression_o ;

expression_o : '-' expression_t
            | expression_t '\''
            | expression_t ;

expression_t : ID '(' params? ')'
             | ID '_' '{' ID '=' expression '}' '^' '{' expression '}' '{' expression '}'
             | '(' expression ')'
             | ID ( '_' set_index } )?
             | num ;

params : expression ( ',' expression )* ;

feed : ID (',' ID )* '~' ( string | ID '(' params? ')' ) ;

evaluation  : 'min' '_' '{' ID ( ',' ID )* '}' '{' expression '}' | expression ;

num : INT | FLOAT ;

ID : [a-zA-Z][a-zA-Z0-9]* ;

STRING : '"' (~["])+ '"' ;

INT : [0-9]+ ;

FLOAT : [0-9]+'.'[0-9]+ ;

WS : [ \t\r\n]+ -> skip ;
*/

program : statement* ;

index
    returns [
        Expression expr
    ]
    :
        ID { $expr = new Expression($ID.text); }
    |
        INT { $expr = new Expression(Integer.valueOf($INT.text)); }
    ;

set_index
    returns [
        ArrayList<Expression> sp = new ArrayList<>()
    ]
    :
        '{'
            a=expression { $sp.add($a.expr); }
            (',' b=expression { $sp.add($b.expr); } )*
        '}'
    |
        c=index { $sp.add($c.expr); }
    ;

var_desc
    returns [
        HashMap<String, Variable> vars = new HashMap<>()
    ]
    :
        a=var_range { $vars.put($a.i, $a.var); }
        (
            ',' b=var_range { $vars.put($b.i, $b.var); }
        )*
    ;

var_range
    returns [
        String i,
        Variable var
    ]
    :
        ID '=' a=expression '...' b=expression
        {
            $i = $ID.text;
            $var = new Variable($a.expr, $b.expr);
        }
    ;

statement : const_def | schema | assignment | feed | evaluation ;

const_def :
        ID
        ':='
        (
            INT { Constants.set($ID.text, Integer.valueOf($INT.text)); }
        |
            FLOAT { Constants.set($ID.text, Float.valueOf($FLOAT.text)); }
        )
    ;

schema
    locals [
        ArrayList<Definition> defs = new ArrayList<>(),
        ArrayList<Expression> sp = new ArrayList<>(),
        DataType dtype
    ]
    :
        a=tensor { $defs.add($a.t); }
        ( ',' b=tensor { $defs.add($b.t); } )*
        'in'
        ID
        {
            if ($ID.text.equals("R")) {
                $dtype = DataType.FLOAT;
            }
            else {
                $dtype = DataType.INT;
            }
        }
        ( '^' set_index { $sp = $set_index.sp; } )?
        ( ',' v=var_desc { for (Definition d : $defs) d.setVar($v.vars); } )?
        {
            for (Definition d : $defs) {
                d.setDtype($dtype);
                d.setShape($sp);
                d.finish();
            }
        }
    ;

tensor
    returns [
        Definition t
    ]
    locals [
        ArrayList<String> idx = new ArrayList<>()
    ]
    :
        a=ID { $t = new Definition($a.text); }
        (
            '_'
            (
                b=ID { $idx.add($b.text); }
            |
                '{'
                c=ID { $idx.add($c.text); }
                ( ',' d=ID { $idx.add($d.text); } )*
                '}'
            )
        )?
        { $t.setIndex($idx); }
    ;

assignment
    locals [
        Assignment a
    ]
    :
        ID { $a = new Assignment($ID.text); $a.setIndex(new ArrayList<Expression>()); }
        ( '_' set_index { $a.setIndex($set_index.sp); } )?
        '='
        expression { $a.setValue($expression.expr); }
        ( ',' var_desc { $a.setVar($var_desc.vars); } )?
        { $a.finish(); }
    ;

expression
    returns[
        Expression expr
    ]
    :
        a=expression '+' b=expression_m
        {
            $expr = new Expression("add", new Expression[]{$a.expr, $b.expr});
        }
    |
        a=expression '-' b=expression_m
        {
            $expr = new Expression("sub", new Expression[]{$a.expr, $b.expr});
        }
    |
        expression_m
        {
            $expr = $expression_m.expr;
        }
    ;

expression_m
    returns [
        Expression expr
    ]
    :
        a=expression_m '*' b=expression_o
        {
            $expr = new Expression("mul", new Expression[]{$a.expr, $b.expr});
        }
    |
        a=expression_m '.' b=expression_o
        {
            $expr = new Expression("matmul", new Expression[]{$a.expr, $b.expr});
        }
    |
        a=expression_m '/' b=expression_o
        {
            $expr = new Expression("div", new Expression[]{$a.expr, $b.expr});
        }
    |
        expression_o
        {
            $expr = $expression_o.expr;
        }
    ;

expression_o
    returns [
        Expression expr
    ]
    :
        '-' a=expression_t
        {
            $expr = new Expression ("neg", new Expression[]{$a.expr});
        }
    |
        a=expression_t '\''
        {
            $expr = new Expression("trans", new Expression[]{$a.expr});
        }
    |
        expression_t
        {
            $expr = $expression_t.expr ;
        }
    ;

expression_t
    returns[
        Expression expr
    ]
    locals[
        ArrayList<String> vars = new ArrayList<>(),
        ArrayList<Expression> idx = new ArrayList<>()
    ]
    :
        ID '(' params ')'
        {
            $expr = new Expression($ID.text, $params.args);
        }
    |
        f=ID '_' '{' a=ID '=' c=expression '}' '^' '{' d=expression '}' '{' e=expression '}'
        {
            $expr = new Expression($f.text, $a.text, new Variable($c.expr, $d.expr), new Expression[]{$e.expr});
        }
    |
        '(' expression ')'
        {
            $expr = $expression.expr;
        }
    |
        ID ( '_' set_index { $idx = $set_index.sp; } )?
        {
            $expr = new Expression($ID.text);
            if ($idx.size() > 0) {
                $idx.add(0, $expr);
                $expr = new Expression("extr", $idx.toArray(new Expression[0]));
            }
        }
    |
        num
        {
            if($num.isInt)
            {
                $expr = new Expression(Integer.valueOf($num.text));
            }
            else
            {
                $expr = new Expression(Float.valueOf($num.text));
            }
        }
    ;

params
    returns [
        Expression[] args
    ]
    locals [
        ArrayList<Expression> pars = new ArrayList<>()
    ]
    :
        a = expression { $pars.add($a.expr); }
        ( ',' b = expression { $pars.add($b.expr); } )*
        {
            $args = $pars.toArray(new Expression[0]);
        }
    ;

feed
    locals[
        ArrayList<String> ids = new ArrayList<>(),
        ArrayList<Expression> args = new ArrayList<>()
    ]
    :
        a=ID { $ids.add($a.text); }
        (',' b=ID { $ids.add($b.text); } )*
        '~'
        (
            string
            { for (String id : $ids) Processor.copyFromFile(id, $string.str); }
        |
            ID
            '('
            ( params { for (Expression e : $params.args) $args.add(e); } )?
            ')'
            { for (String id : $ids) Processor.copyFromFunction(id, $ID.text, $args.toArray(new Expression[0])); }
        )
    ;

eval
    returns [
        Query qr
    ]
    locals [
        ArrayList<String> args = new ArrayList<>()
    ]
    :
        'min' '_' '{' a=ID { $args.add($a.text); } ( ',' b=ID { $args.add($b.text); } )* '}'
        '{' c=eval '}'
        { $qr = new Query(TaskType.MINIMIZE, $args, $c.qr); }
    |
        'max' '_' '{' a=ID { $args.add($a.text); } ( ',' b=ID { $args.add($b.text); } )* '}'
        '{' c=eval '}'
        { $qr = new Query(TaskType.MAXIMIZE, $args, $c.qr); }
    |
        expression
        { $qr = new Query($expression.expr); }
    ;

evaluation
    locals [
        ArrayList<String> args = new ArrayList<>()
    ]
    :
        'min' '_' '{' a=ID { $args.add($a.text); } ( ',' b=ID { $args.add($b.text); } )* '}'
        '{' eval '}'
        { Processor.evaluate(new Query(TaskType.MINIMIZE, $args, $eval.qr)); }
    |
        'max' '_' '{' a=ID { $args.add($a.text); } ( ',' b=ID { $args.add($b.text); } )* '}'
        '{' eval '}'
        { Processor.evaluate(new Query(TaskType.MAXIMIZE, $args, $eval.qr)); }
    |
        expression
        { Processor.evaluate(new Query($expression.expr)); }
    ;

num
    returns [
        Boolean isInt
    ]
    :
        INT { $isInt = true; }
    |
        FLOAT { $isInt = false; }
    ;

string
    returns [
        String str
    ]
    :
        STRING { $str = $STRING.text.substring(1, $STRING.text.length() - 1); }
    ;

ID : [a-zA-Z][a-zA-Z0-9]* ;

STRING : '"' (~["])+ '"' ;

INT : [0-9]+ ;

FLOAT : [0-9]+'.'[0-9]+ ;

WS : [ \t\r\n]+ -> skip ;