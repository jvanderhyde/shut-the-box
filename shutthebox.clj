;;Monte Carlo tree search for Shut the Box

;;Task 3
(defn dice-roll []
  (+ 2 (rand-int 6) (rand-int 6)))
(dice-roll)

(def start-box
  (set (range 1 10)))
(def sample-box (disj sample-box 1 3 6))
sample-box

;;Task 4
(defn possible-moves [box roll]
  (if (contains? box roll)
    #{(disj box roll)}
    #{}))
(possible-moves sample-box 4)
(possible-moves sample-box 3)

