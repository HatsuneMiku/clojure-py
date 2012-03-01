# mrmekon's fork

I hate it when people don't explain what their fork is.

This is a fork of clojure-py shortly after it was announced on HackerNews.  I wanted to write a script that would determine whether it was operating on the JVM or in Python.  I made a few small changes to allow that.

I don't know clojure well, nor the implementation of clojure-py.  I changed the way 'str' is handled in clojure-py so it will handle a class type, and I added a temporary 'println' implementation. 

My test script simply checks the class of a built-in, converts it to a string, and compares it to known strings.


# clojure-py

An implementation of Clojure in pure Python.

## Why Python? 

It is our belief that static virtual machines make very poor runtimes for dynamic languages. They constrain the languages to their view of what the "world should look like" and limit the options available to language implementors. We are attempting to prove this by writing an implementation of Clojure that runs on the Python VM. We believe that with a proper dynamic JIT (like pypy) a version of clojure running on a dynamic VM can outperform its JVM and CLR counterparts. 

Aside from that, there are many Python libraries like PySide (Qt GUI), numpy, scipy, and stackless that do not have JVM counterparts, or at least the Python implemntations are easier to use and learn. clojure-py will integrate tightly with thy Python VM and will be able to use all of these libraries.

## Basic concepts

Python builtins are available under the py/ namespace. Actual python bytecodes can be injected via py.bytecodes/OP

Viewing the code at https://github.com/halgari/clojure-py/blob/master/clojure/core.clj is probably the best way to get a feeling of what is possible, and how clojure-py implements certain functions.

One note: clojure-py implements the new "property vs calling method" design used in ClojureScript:

      (.__name__ (module)) ; same as module.__name__() in python
   
      (.-__name__ (module)) ; same as module.__name__ in python
   

## How can I help?

Check out the Wiki for more information about the roadmap for this project. Then check out the issues list for any items marked "isolated change". These are changes that should be somewhat easy for a newcommer to pick up and will not involve messing around with the internals of the implementation much. Also feel free to join our [mailing list](http://groups.google.com/group/clojure-py-dev)

## Blog
   From time to time, we'll post status updates, ideas and plans to this blog http://clojure-py.blogspot.com/

## Installation

    ./setup.py develop  # or ./setup.py install for 'production'

## Unit tests

    # (must 'easy_install nose' or 'pip install nose' first)
    nosetests

## Running

    clojurepy
    
## License
Not endorsed by Rich Hickey, but this project contains code based on his work

 Clojure-Py
 Copyright (c) Rich Hickey. All rights reserved.
 The use and distribution terms for this software are covered by the
 Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 which can be found in the file epl-v10.html at the root of this distribution.
 By using this software in any fashion, you are agreeing to be bound by
 the terms of this license.
 You must not remove this notice, or any other, from this software.
