(ns stocksim.core
  (:require [clojure.core.async
             :refer [chan >! <! timeout go]
              :as async]))


(defn adjust-price [old-price]
  (let  [numerator (- (rand-int 30) 15)
         adjustment (* numerator 0.01M)]
    (+ old-price adjustment)))

(defn random-time [t]
  (* t (+ 1 (rand-int 5))))

(defn new-transaction [symbol price]
  {:symbol symbol
   :time (java.util.Date.) 
   :price price})

(defn make-ticker [symbol t start-price]
  (let [c (chan)]
    (go
     (loop [price start-price]
       (let [new-price (adjust-price price)]
         (<! (timeout (random-time t)))
         (>! c (new-transaction symbol new-price))
         (recur new-price))))
    c))

(def stocks [ ;; symbol min-interval starting-price
             ["TSLA" 1000 214]
             ["YHOO" 3200 345]
             ["CSCO" 630  22]
             ["PSMT" 2200 55]
             ["GOOG" 5800 1127]
             ["IBM" 2703  192]
             ["MSFT" 306 40]
             ["ORCL" 4200 39]
             ["T" 700 35]])

(defn exec-sim []
  (let [ticker (async/merge
                (map #(apply make-ticker %) stocks))]
    (go
     (loop [x 0]
       (when (< x 400)
         (do (println (str x "-" (<! ticker)))
             (recur (inc x))))))))

(defn -main
  []
  (exec-sim))
