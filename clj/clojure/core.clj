;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns ^{:doc "The core Clojure language."
       :author "Rich Hickey"}
  clojure.core)

(def unquote)
(def unquote-splicing)

(def
 ^{:arglists '([& items])
   :doc "Creates a new list containing the items."
   :added "1.0"}
  list clojure.lang.persistentlist.PersistentList.creator)

(def
 ^{:arglists '([& items])
   :doc "Creates a new vector containing the items."
   :added "1.0"}
  vector clojure.lang.rt.vector)

(def
 #^{:arglists '([x seq])
    :doc "Returns a new seq where x is the first element and seq is
    the rest."}
 cons (fn* cons [x seq] (. clojure.lang.rt (cons x seq))))

;during bootstrap we don't have destructuring let, loop or fn, will redefine later
(def
  ^{:macro true
    :added "1.0"}
  let (fn* let [&form &env & decl] (cons 'let* decl)))

(def
 ^{:macro true
   :added "1.0"}
 loop (fn* loop [&form &env & decl] (cons 'loop* decl)))

(def
 ^{:macro true
   :added "1.0"}
 fn (fn* fn [&form &env & decl] 
         (.withMeta (cons 'fn* decl) 
                    (.meta &form))))

(def
    ^{:arglists '([& args])
      :doc "Returns a new python tuple from args"
      :added "1.0"}
 tuple (fn native-list [& args] args))

(def
    ^{:arglists '([& args])
      :doc "Clojure version of RT.assoc"
      :added "1.0"}
 _assoc (fn* assoc [col k v]
                   (py/if col
                      (.assoc col k v)
                      (clojure.lang.rt.map k v))))

(def
 ^{:arglists '(^clojure.lang.ISeq [coll])
   :doc "Returns a seq on the collection. If the collection is
    empty, returns nil.  (seq nil) returns nil. seq also works on
    Strings, native Python lists and any objects
    that implement __getitem__."
   :tag clojure.lang.ISeq
   :added "1.0"
   :static true}
 seq (fn seq [coll] (. clojure.lang.rt (seq coll))))

(def
 ^{:arglists '([^Class c x])
   :doc "Evaluates x and tests if it is an instance of the class
    c. Returns true or false"
   :added "1.0"}
 instance? (fn instance? [c x] (py/isinstance x c)))

(def
 ^{:arglists '([x])
   :doc "Return true if x implements ISeq"
   :added "1.0"
   :static true}
 seq? (fn seq? [x] (instance? clojure.lang.iseq.ISeq x)))

(def
 ^{:arglists '([coll])
   :doc "Returns the first item in the collection. Calls seq on its
    argument. If coll is nil, returns nil."
   :added "1.0"
   :static true}
 first (fn first [s]
	   (py/if (py.bytecode/COMPARE_OP "is not" s nil)
	     (py/if (instance? ISeq s)
	       (.first s)
	       (let [s (seq s)]
	            (py/if (py.bytecode/COMPARE_OP "is not" s nil)
	                   (.first s)
			   nil)))
	    nil)))

(def
 ^{:arglists '([coll])
   :tag clojure.lang.ISeq
   :doc "Returns a seq of the items after the first. Calls seq on its
  argument.  If there are no more items, returns nil."
   :added "1.0"
   :static true}  
 next (fn next [s]
 	 			 (py/if (is? nil s)
 	 			 	 nil
					 (py/if (instance? ISeq s)
						 (.next s)
						 (let [s (seq s)]
							  (.next s))))))

(def
 ^{:arglists '([coll])
   :tag clojure.lang.ISeq
   :doc "Returns a possibly empty seq of the items after the first. Calls seq on its
  argument."
   :added "1.0"
   :static true}  
 rest (fn rest [x] (py/if (py/isinstance x ISeq)
                       (.more x)
                       (let [s (seq x)]
                           (py/if s
                               (.more s)
                               clojure.lang.persistentlist.EMPTY)))))

(def
 ^{:doc "Same as (first (next x))"
   :arglists '([x])
   :added "1.0"
   :static true}
 second (fn second [x] (first (next x))))

(def
 ^{:doc "Same as (first (first x))"
   :arglists '([x])
   :added "1.0"
   :static true}
 ffirst (fn ffirst [x] (first (first x))))

(def
 ^{:doc "Same as (next (first x))"
   :arglists '([x])
   :added "1.0"
   :static true}
 nfirst (fn nfirst [x] (next (first x))))

(def
 ^{:doc "Same as (first (next x))"
   :arglists '([x])
   :added "1.0"
   :static true}
 fnext (fn fnext [x] (first (next x))))

(def
 ^{:doc "Same as (next (next x))"
   :arglists '([x])
   :added "1.0"
   :static true}
 nnext (fn nnext [x] (next (next x))))

(def
 ^{:arglists '([x])
   :doc "Return true if x is a String"
   :added "1.0"
   :static true}
 string? (fn string? [x] (instance? py/str x)))

(def
 ^{:arglists '([x])
   :doc "Return true if x implements IPersistentMap"
   :added "1.0"
   :static true}
 map? (fn ^:static map? [x] (instance? clojure.lang.ipersistentmap.IPersistentMap x)))

(def
 ^{:arglists '([x])
   :doc "Return true if x implements IPersistentVector"
   :added "1.0"
   :static true}
 vector? (fn vector? [x] (instance? clojure.lang.ipersistentvector.IPersistentVector x)))

(def
 ^{:arglists '([map key val] [map key val & kvs])
   :doc "assoc[iate]. When applied to a map, returns a new map of the
    same (hashed/sorted) type, that contains the mapping of key(s) to
    val(s). When applied to a vector, returns a new vector that
    contains val at index. Note - index must be <= (count vector)."
   :added "1.0"}
 assoc
 (fn assoc
   ([map key val] (_assoc map key val))
   ([map key val & kvs]
    (let [ret (assoc map key val)]
      (py/if kvs
        (recur ret (first kvs) (second kvs) (nnext kvs))
        ret)))))

;;;;;;;;;;;;;;;;; metadata ;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def
 ^{:arglists '([obj])
   :doc "Returns the metadata of obj, returns nil if there is no metadata."
   :added "1.0"}
 meta (fn meta [x]
        (py/if (py/hasattr x "meta")
          (.meta x))))

(def
 ^{:arglists '([obj m])
   :doc "Returns an object of the same type and value as obj, with
    map m as its metadata."
   :added "1.0"}
 with-meta (fn with-meta [x m]
             (. x (withMeta m))))

;;;;;;;;;;;;;;;;

(def 
 ^{:arglists '([coll])
   :doc "Return the last item in coll, in linear time"
   :added "1.0"}
 last (fn last [s]
        (py/if (next s)
          (recur (next s))
          (first s))))

(def nil?
 ^{:tag Boolean
   :doc "Returns true if x is nil, false otherwise."
   :added "1.0"
   :static true}
  (fn nil? [x] (is? x nil)))

(def
 ^{:arglists '([& args])
   :doc "Clojure version of RT.conj"
   :added "1.0"}
 _conj (fn _conj [coll x] (py/if (nil? coll)
                            clojure.lang.persistentlist.EMPTY
                            (.cons coll x))))


(def
 ^{:arglists '([coll x] [coll x & xs])
   :doc "conj[oin]. Returns a new collection with the xs
    'added'. (conj nil item) returns (item).  The 'addition' may
    happen at different 'places' depending on the concrete type."
   :added "1.0"}
 conj (fn conj 
        ([coll x] (_conj coll x))
        ([coll x & xs]
         (py/if (nil? xs)
             (conj coll x)
             (recur (conj coll x) (first xs) (next xs))))))



(def 
 ^{:arglists '([coll])
   :doc "Return a seq of all but the last item in coll, in linear time"
   :added "1.0"}
 butlast (fn butlast [s]
           (loop [ret [] s s]
             (py/if (nil? (next s))
               (seq ret)  
               (recur (conj ret (first s)) (next s))))))

 		
(def set-macro 
    (fn set-macro [f]
        (py/setattr f "macro?" true)
        f))	 
 	 
 	 
(def ^{:private true :dynamic true}
  assert-valid-fdecl (fn [fdecl]))

(def
 ^{:private true}
 sigs
 (fn [fdecl]
   (assert-valid-fdecl fdecl)
   (let [asig 
         (fn [fdecl]
           (let [arglist (first fdecl)
                 ;elide implicit macro args
                 arglist (py/if (.__eq__ '&form (first arglist)) 
                           (clojure.lang.rt.subvec arglist 2 (py/len arglist))
                           arglist)
                 body (next fdecl)]
             (py/if (map? (first body))
               (py/if (next body)
                 (with-meta arglist (conj (py/if (meta arglist) (meta arglist) {}) (first body)))
                 arglist)
               arglist)))]
     (py/if (seq? (first fdecl))
       (loop [ret [] fdecls fdecl]
         (py/if fdecls
           (recur (conj ret (asig (first fdecls))) (next fdecls))
           (seq ret)))
       (list (asig fdecl))))))



(def 

 ^{:doc "Same as (def name (fn [params* ] exprs*)) or (def
    name (fn ([params* ] exprs*)+)) with any doc-string or attrs added
    to the var metadata. prepost-map defines a map with optional keys
    :pre and :post that contain collections of pre or post conditions."
   :arglists '([name doc-string? attr-map? [params*] prepost-map? body]
                [name doc-string? attr-map? ([params*] prepost-map? body)+ attr-map?])
   :added "1.0"}
 defn (fn defn [&form &env name & fdecl]
        (let [m (py/if (string? (first fdecl))
                  {:doc (first fdecl)}
                  {})
              fdecl (py/if (string? (first fdecl))
                      (next fdecl)
                      fdecl)
              m (py/if (map? (first fdecl))
                  (conj m (first fdecl))
                  m)
              fdecl (py/if (map? (first fdecl))
                      (next fdecl)
                      fdecl)
              fdecl (py/if (vector? (first fdecl))
                      (list fdecl)
                      fdecl)
              m (py/if (map? (last fdecl))
                  (conj m (last fdecl))
                  m)
              fdecl (py/if (map? (last fdecl))
                      (butlast fdecl)
                      fdecl)
              m (conj {:arglists (list 'quote (sigs fdecl))} m)
              m (let [inline (:inline m)
                      ifn (first inline)
                      iname (second inline)]
                  ;; same as: (py/if (and (= 'fn ifn) (not (symbol? iname))) ...)
                  (py/if (py/if (.__eq__ 'fn ifn)
                        (py/if (instance? clojure.lang.symbol.Symbol iname) false true))
                    ;; inserts the same fn name to the inline fn if it does not have one
                    (assoc m :inline (cons ifn (cons (clojure.lang.symbol.Symbol/intern (.concat (.getName name) "__inliner"))
                                                     (next inline))))
                    m))
              m (conj (py/if (meta name) (meta name) {}) m)
              ]
          (list 'def (with-meta name m)
                ;;todo - restore propagation of fn name
                ;;must figure out how to convey primitive hints to self calls first
                (cons `fn fdecl)))))

(set-macro defn)


(defn vec
  "Creates a new vector containing the contents of coll."
  {:added "1.0"
   :static true}
  ([coll]
    (py/if (nil? coll)
        nil
        (clojure.lang.persistentvector.vec coll))))

(def
 ^{:doc "Like defn, but the resulting function name is declared as a
  macro and will be used as a macro by the compiler when it is
  called."
   :arglists '([name doc-string? attr-map? [params*] body]
                 [name doc-string? attr-map? ([params*] body)+ attr-map?])
   :added "1.0"}
 defmacro (fn [&form &env
                name & args]
             (let [prefix (loop [p (list name) args args]
                            (let [f (first args)]
                              (py/if (string? f)
                                (recur (cons f p) (next args))
                                (py/if (map? f)
                                  (recur (cons f p) (next args))
                                  p))))
                   fdecl (loop [fd args]
                           (py/if (string? (first fd))
                             (recur (next fd))
                             (py/if (map? (first fd))
                               (recur (next fd))
                               fd)))
                   fdecl (py/if (vector? (first fdecl))
                           (list fdecl)
                           fdecl)
                   add-implicit-args (fn [fd]
                             (let [args (first fd)]
                               (cons (vec (cons '&form (cons '&env args))) (next fd))))
                   add-args (fn [acc ds]
                              (py/if (nil? ds)
                                acc
                                (let [d (first ds)]
                                  (py/if (map? d)
                                    (conj acc d)
                                    (recur (conj acc (add-implicit-args d)) (next ds))))))
                   fdecl (seq (add-args [] fdecl))
                   decl (loop [p prefix d fdecl]
                          (py/if p
                            (recur (next p) (cons (first p) d))
                            d))]
               (list 'do
                     (cons `defn decl)
                     (list 'set-macro name)
                     name))))


(set-macro defmacro)


(defmacro when
  "Evaluates test. If logical true, evaluates body in an implicit do."
  {:added "1.0"}
  [test & body]
  (list 'py/if test (cons 'do body)))

(defmacro when-not
  "Evaluates test. If logical false, evaluates body in an implicit do."
  {:added "1.0"}
  [test & body]
    (list 'py/if test nil (cons 'do body)))

(defn false?
  "Returns true if x is the value false, false otherwise."
  {:added "1.0"}
  [x] (.__eq__ x false))

(defn true?
  "Returns true if x is the value true, false otherwise."
  {:added "1.0"}
  [x] (.__eq__ x true))

(defn not
  "Returns true if x is logical false, false otherwise."
  {:added "1.0"}
  [x] (py/if x false true))

(defn str
  "With no args, returns the empty string. With one arg x, returns
  x.__str__().  (str nil) returns the empty string. With more than
  one arg, returns the concatenation of the str values of the args."
  {:added "1.0"}
  ([] "")
  ([x]
   (py/if (nil? x) "" (.__str__ x)))
  ([x & ys]
     (let [lst (py/list (.__str__ x))
           lst (loop [remain ys]
                 (py/if remain
                   (do (.append lst (.__str__ (first remain)))
                       (recur (next remain)))
                   lst))]
           (.join "" lst))))

(defn symbol?
  "Return true if x is a Symbol"
  {:added "1.0"}
  [x] (instance? clojure.lang.symbol.Symbol x))

(defn keyword?
  "Return true if x is a Keyword"
  {:added "1.0"}
  [x] (instance? clojure.lang.cljkeyword.Keyword x))

(defn symbol
  "Returns a Symbol with the given namespace and name."
  {:tag clojure.lang.Symbol
   :added "1.0"}
  ([name] (py/if (symbol? name) name (clojure.lang.symbol.Symbol.intern name)))
  ([ns name] (clojure.lang.symbol.Symbol.intern ns name)))


(defn inc
  "Returns a number one greater than num. Does not auto-promote
  longs, will throw on overflow. See also: inc'"
  {:added "1.2"}
  [x] (.__add__ x 1))

(defn +
  [x y] (.__add__ x y))

(defn hash-map
  "keyval => key val
  Returns a new hash map with supplied mappings."
  {:added "1.0"}
  ([] {})
  ([& keyvals]
      (let [coll {}]
          (loop [keyvals (seq keyvals) coll coll]
              (py/if (nil? keyvals)
                  coll
                  (do (py/if (.__eq__ (py/len keyvals) 1)
                          (throw (py/Exception "Even number of args required to hash-map")))
                      (py/if (contains? coll (first keyvals))
                          (throw (py/Exception "Duplicate keys found in hash-map")))
                      (recur (nnext keyvals) 
                             (.assoc coll 
                                    (first keyvals)
                                    (fnext keyvals)))))))))
          
      


(defn gensym
  "Returns a new symbol with a unique name. If a prefix string is
  supplied, the name is prefix# where # is some unique number. If
  prefix is not supplied, the prefix is 'G__'."
  {:added "1.0"}
  ([] (gensym "G__"))
  ([prefix-string] (. clojure.lang.symbol.Symbol (intern (py/str prefix-string (py/str (. clojure.lang.rt (nextID))))))))


(defmacro cond
  "Takes a set of test/expr pairs. It evaluates each test one at a
  time.  If a test returns logical true, cond evaluates and returns
  the value of the corresponding expr and doesn't evaluate any of the
  other tests or exprs. (cond) returns nil."
  {:added "1.0"}
  [& clauses]
    (when clauses
      (list 'py/if (first clauses)
            (py/if (next clauses)
                (second clauses)
                (throw (IllegalArgumentException.
                         "cond requires an even number of forms")))
            (cons 'clojure.core/cond (next (next clauses))))))


(defn spread
  {:private true}
  [arglist]
  (cond
   (nil? arglist) nil
   (nil? (next arglist)) (seq (first arglist))
   :else (cons (first arglist) (spread (next arglist)))))

(defn list*
  "Creates a new list containing the items prepended to the rest, the
  last of which will be treated as a sequence."
  {:added "1.0"}
  ([args] (seq args))
  ([a args] (cons a args))
  ([a b args] (cons a (cons b args)))
  ([a b c args] (cons a (cons b (cons c args))))
  ([a b c d & more]
     (cons a (cons b (cons c (cons d (spread more)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn =
  "Equality. Returns true if x equals y, false if not. Same as
  Java x.equals(y) except it also works for nil, and compares
  numbers and collections in a type-independent manner.  Clojure's immutable data
  structures define equals() (and thus =) as a value, not an identity,
  comparison."
  {:added "1.0"}
  ([x] true)
  ([x y] (py.bytecode/COMPARE_OP "==" x y))
  ([x y & more]
   (py/if (py.bytecode/COMPARE_OP "==" x y)
     (py/if (next more)
       (recur y (first more) (next more))
       (py.bytecode/COMPARE_OP "==" y (first more)))
     false)))


(defn not=
  "Same as (not (= obj1 obj2))"
  {:added "1.0"}
  ([x] false)
  ([x y] (not (= x y)))
  ([x y & more]
   (not (apply = x y more))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-class
    "Creates a new clas with the given name, that is inherited from
    classes and has the given member functions."
    [name classes members]
    (py/type (.-name name) (apply tuple (conj classes py/object)) (.toDict members)))

(defn make-init
    "Creates a __init__ method for use in deftype"
    [fields]
    (loop [fields fields
           args ['self]
           body []]
           (py/if (not fields)
               (cons 'fn (cons '__init__ (cons args body)))
               (let [newargs (conj args (first fields))
                     newbody (conj body (list 'py/setattr
                                              'self 
                                              (py/str (first fields))
                                              (first fields)))]
                     (recur (next fields) newargs newbody)))))

(defn make-props
    [fields selfname]
    (loop [remain (seq fields)
        props []]
       (py/if (nil? remain)
           props
           (recur (next remain) 
                  (conj (conj props (first remain))
                        (list 'getattr selfname (.-name (first remain))))))))

(defn prop-wrap-fn
    [members f]
    (list 'fn 
 	  (first f) 
	  (second f)
          (list* 'let-macro 
		(make-props members 
			    (first (fnext f)))
		(next (next f)))))

(defn prop-wrap-multi
    [members f]
    (let [name (first f)
          f (next f)
          wrapped (loop [remain f
                         wr []]
			(py/if remain
			    (let [cur (first remain)
				   args (first cur)
				   body (next cur)]
				  (recur (next remain) 
 			               (cons (list args
					          (list* 'let-macro
						      (make-props members
								  (first args))
						      body))
					     wr)))
			    wr))]
	(list* 'fn name wrapped)))
                        
         
(defn prop-wrap
    [members f]
    (if (vector? (fnext f))
	(prop-wrap-fn members f)
	(prop-wrap-multi members f)))


(defmacro deftype
    [name fields & specs]
    (loop [specs (seq specs)
           inherits []
           fns {"__init__" (make-init fields)}]
          (cond (not specs)
                    (list 'def name (list 'make-class (list 'quote name) inherits fns))
                (symbol? (first specs))
                    (recur (next specs) 
                           (conj inherits (first specs))
                           fns)
                (instance? clojure.lang.ipersistentlist.IPersistentList (first specs))
                    (recur (next specs)
                           inherits
                           (assoc fns (py/str (ffirst specs))
                           	   	      (prop-wrap fields (first specs)))))))
(def definterface deftype) 

;;;;;;;;;;;;;;;;;Lazy Seq and Chunked Seq;;;;;;;;;;;;;;;;


(definterface IPending []
	(isRealized [self] nil))

(deftype LazySeq [fnc sv s _meta]
	(withMeta [self meta]
		(LazySeq nil nil (.seq self) meta))
	(sval [self]
		(when (not (nil? fnc))
			  (setattr self "sv" (fnc))
			  (setattr self "fnc" nil))
		(py/if (not (nil? sv))
			sv
		s))
	clojure.lang.iseq.ISeq
	(seq [self]
		(.sval self)
		(when (not (nil? sv))
		      (let [ls sv]
		           (setattr self "sv" nil)
          		   (setattr self "s"
       		 	 	         (loop [ls ls]
					   (py/if (instance? LazySeq ls)
					          (recur (.sval ls))
					          (seq ls))))))
		s)
	(__len__ [self]
	    (loop [c 0
	           s (.seq self)]
	          (py/if (nil? s)
	              c
	              (recur (.__add__ c 1) (next s)))))
	(first [self]
	    (.seq self)
	    (py/if (nil? s)
	        nil
	        (.first s)))
	(next [self]
	    (.seq self)
	    (py/if (nil? s)
	        nil
	        (.next s)))
	(more [self]
	    (.seq self)
	    (py/if (nil? s)
	        (list)
	        (.more self)))
	(cons [self o]
	    (cons o (.seq self)))
	(empty [self]
	    (list)))

(defmacro lazy-seq
  "Takes a body of expressions that returns an ISeq or nil, and yields
  a Seqable object that will invoke the body only the first time seq
  is called, and will cache the result and return it on all subsequent
  seq calls. See also - realized?"
  {:added "1.0"}
  [& body]
  (list 'clojure.core.LazySeq (list* '^{:once true} fn* [] body) nil nil nil))    


(definterface IChunkedSeq [] 
	clojure.lang.sequential.Sequential
	clojure.lang.iseq.ISeq
	(chunkedFirst [self] nil)
	(chunkedNext [self] nil)
	(chunkedMore [self] nil))


(deftype ChunkBuffer [buffer end]
    (add [self o]
        (setattr self "end" (inc end))
        (py.bytecode/STORE_SUBSCR buffer end o))
    (chunk [self]
        (let [ret (ArrayChunk buffer 0 end)]
             (setattr self "buffer" nil)
             ret))
    (count [self] end))

(deftype ArrayChunk [array off end]
    (__getitem__ ([self i]
          (get array (inc of)))
         ([self i not-found]
	  (if (py.bytecode/COMPARE_OP ">=" i 0)
	      (if (py.bytecode/COMPARE_OP "<" i (len self))
		  (nth self i)
		  not-found)
	      not-found)))

    (__len__ [self]
        (py.bytecode/BINARY_SUBTRACT end off))

    (dropFirst [self]
	(if (= off end)
	    (throw (IllegalStateException "dropFirst of empty chunk")))
	(ArrayChunk array (inc off) end))

    (reduce [self f start]
	(loop [ret (f start (get array off))
	       x (inc off)]
	     (if (py.bytecode/COMPARE_OP "<" x end)
		 (recur (f ret (get array x)) 
			(inc x))
		 ret))))



(deftype ChunkedCons [_meta chunk _more]

	clojure.lang.aseq.ASeq
	(first [self]
	       (.nth chunk 0))
	(withMeta [self meta]
	  (if (py.bytecode/COMPARE_OP "is" meta _meta)
	        (ChunkedCons meta chunk _more)
		self))


	(next [self]
	  (if (py.bytecode/COMPARE_OP ">" (len chunk) 1)
	      (ChunkedCons nil (.dropFirst chunk) _more)
	      (.chunkedNext self)))

	(more [self]
	  (cond (py.bytecode/COMPARE_OP ">" (len chunk) 1)
		  (ChunkedCons nil (.dropFirst chunk) _more)
		(py.bytecode/COMPARE_OP "is" _more nil)
		  '()
		:else
		  _more))

	IChunkedSeq
	(chunkedFirst [self] chunk)

	(chunkedNext [self]
	  (.seq (.chunkedMore self)))

	(chunkedMore [self]
	  (if (is? _more nil)
	        '()
		_more)))




(defn chunk-buffer [capacity]
     (ChunkBuffer (py.bytecode/BINARY_MULTIPLY (list [None]) capacity)
		  0))
(defn chunk-append [b x]
     (.add b x))

(defn chunk [b]
     (.chunk b))

(defn chunk-first [s]
     (.chunkedFirst s))

(defn chunk-rest [s]
     (.chunkedMore s))

(defn chunk-next [s]
     (.chunkedNext s))

(print "foo" (dir ChunkedCons))
(defn chunk-cons [chunk rest]
     (if (= (len chunk) 0)
	 rest
	 (ChunkedCons chunk rest)))

(defn chunked-seq? [s]
     (instance? IChunkedSeq s))


(defn concat
  "Returns a lazy seq representing the concatenation of the elements in the supplied colls."
  {:added "1.0"}
  ([] (lazy-seq nil))
  ([x] (lazy-seq x))
  ([x y]
    (lazy-seq
      (let [s (seq x)]
        (if s
          (if (chunked-seq? s)
            (chunk-cons (chunk-first s) (concat (chunk-rest s) y))
            (cons (first s) (concat (rest s) y)))
          y))))
  ([x y & zs]
     (let [cat (fn cat [xys zs]
                 (lazy-seq
                   (let [xys (seq xys)]
                     (if xys
                       (if (chunked-seq? xys)
                         (chunk-cons (chunk-first xys)
                                     (cat (chunk-rest xys) zs))
                         (cons (first xys) (cat (rest xys) zs)))
                       (when zs
                         (cat (first zs) (next zs)))))))]
       (cat (concat x y) zs))))


(defmacro if-not
  "Evaluates test. If logical false, evaluates and returns then expr, 
  otherwise else expr, if supplied, else nil."
  {:added "1.0"}
  ([test then] `(if-not ~test ~then nil))
  ([test then else]
   `(py/if (not ~test) ~then ~else)))

(defmacro and
  "Evaluates exprs one at a time, from left to right. If a form
  returns logical false (nil or false), and returns that value and
  doesn't evaluate any of the other expressions, otherwise it returns
  the value of the last expr. (and) returns true."
  {:added "1.0"}
  ([] true)
  ([x] x)
  ([x & next]
   `(let [and# ~x]
      (py/if and# (and ~@next) and#))))


