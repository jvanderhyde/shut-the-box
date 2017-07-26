;;Monte Carlo tree search for Shut the Box

;;Task 3
(defn dice-roll []
  (+ 2 (rand-int 6) (rand-int 6)))
(dice-roll)

(def sample-box
  (set (range 1 10)))
(disj sample-box 1 3 6)

