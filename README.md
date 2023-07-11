# Mlog

This is the implementation of the paper [“MLog: Towards Declarative In-Database Machine Learning”](https://dl.acm.org/doi/10.14778/3137765.3137812). 

## Abstract

MLog is a high-level language that integrates machine learning into data management systems. Unlike existing machine learning frameworks (e.g., TensorFlow, Theano, and Caffe), MLog is declarative, in the sense that the system manages all data movement, data persistency, and machine-learning related optimizations (such as data batching) automatically. Our interactive demonstration will show audience how this is achieved based on the novel notion of tensoral views (TViews), which are similar to relational views but operate over tensors with linear algebra. With MLog, users can succinctly specify not only simple models such as SVM (in just two lines), but also sophisticated deep learning models that are not supported by existing in-database analytics systems (e.g., MADlib, PAL, and SciDB), as a series of cascaded TViews. Given the declarative nature of MLog, we further demonstrate how query/program optimization techniques can be leveraged to translate MLog programs into native TensorFlow programs. The performance of the automatically generated Tensor-Flow programs is comparable to that of hand-optimized ones.

## Dependencies:
	jdk 1.8
	maven
	libzmq
	libjzmq

## Usage:
	compile project:	./compile.sh
	clear:			./clear.sh
	run program:		./run.sh <file path>

## Example:
	./compile.sh
	./run.sh imdb.bl


## Citation 

```
@article{10.14778/3137765.3137812,
author = {Li, Xupeng and Cui, Bin and Chen, Yiru and Wu, Wentao and Zhang, Ce},
title = {MLog: Towards Declarative in-Database Machine Learning},
year = {2017},
issue_date = {August 2017},
publisher = {VLDB Endowment},
volume = {10},
number = {12},
issn = {2150-8097},
url = {https://doi.org/10.14778/3137765.3137812},
doi = {10.14778/3137765.3137812},
journal = {Proc. VLDB Endow.},
month = {aug},
pages = {1933–1936},
numpages = {4}
}```
