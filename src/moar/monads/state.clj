(ns moar.monads.state
  (:require [moar.protocols :refer :all]))

(declare state-v make-state)

(defprotocol State
  (run-state [this init-state]
    "given a initial state returns a pair
     where the first value is the modified state
     and the second value is the value produced by running the operation"))

(deftype StateMonad []
  Monad
  (wrap* [_ value] (state-v value))
  (bind* [_ monad fun]
    (make-state
     (fn [state]
       (let [[state value] (run-state monad state)]
         (run-state (fun value) state))))))

(def monad
  "monad implementation for the state monad"
  (StateMonad.))

(deftype StateFunction [fun]
  State
  (run-state [_ init-state] (fun init-state))
  MonadInstance
  (->monad-implementation [_] monad))

(defn make-state
  "constructs a instance of state function given a function"
  [fun]
  (StateFunction. fun))

(defn state-v
  "returns a state function that leaves the state unchanged
   and uses the given value as its value"
  [value]
  (make-state
   (fn [state]
     [state value])))

(defn mod-state
  "takes a function of the state
   and returns a state function of the value nil"
  [fun & args]
  (make-state (fn [state] [(apply fun state args) nil])))

(def get-state
  "reads the state into the value"
  (make-state (fn [state] [state state])))

(defn set-state
  "change the state to the given value"
  [new-state]
  (make-state (fn [state] [new-state nil])))
