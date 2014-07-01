(ns moar.core
  (:require [moar.protocols :refer :all]
            [moar.monads.maybe :as maybe]))

(defn monad-instance?
  "Checks whether monads have the given implementation"
  [impl & monads]
  (every? #(and (satisfies? MonadInstance %)
                (= impl (->monad-implementation %)))
          monads))

(defn same-monad?
  "Checks whether two monads have the same implementation"
  [monad-a monad-b]
  (and
   (satisfies? MonadInstance monad-a)
   (satisfies? MonadInstance monad-b)
   (= (->monad-implementation monad-a)
      (->monad-implementation monad-b))))

(defn bind
  "Applies a function returning a monad to a monad of the same kind"
  ([monad function]
     (bind (->monad-implementation monad) monad function))
  ([impl monad function]
     {:post [(same-monad? monad %)]}
     (bind* impl monad function)))

(defn >>=
  "Applies bind sequentially to n functions"
  [monad & functions]
  (reduce bind monad functions))

(defn- >>-body [[head & tail :as body]]
  {:pre [(>= (count body) 1)]}
  (if (empty? tail)
    head
    `(bind ~head (fn [_#] ~(>>-body tail)))))

(defmacro >>
  "Chain actions returning discarding intermediate results"
  [& actions]
  (>>-body actions))

(defn mplus
  "Mean of combining two monads"
  ([monad-a monad-b]
     (mplus (->monad-implementation monad-a) monad-a monad-b))
  ([impl monad-a monad-b]
     {:pre [(monad-instance? impl monad-a monad-b)]}
     (mplus* impl monad-a monad-b)))
