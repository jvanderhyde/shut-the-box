;;Monte Carlo tree search for Shut the Box

;;Task 3
(defn dice-roll []
  (+ 2 (rand-int 6) (rand-int 6)))
(dice-roll)

(def start-box
  (set (range 1 10)))
(def sample-box (disj start-box 1 3 6))
sample-box
(def easy-box #{7})

(defn win? [box]
  (empty? box))

;;Task 4
(defn possible-moves [box roll]
  (if (contains? box roll)
    #{(disj box roll)}
    #{}))
(possible-moves sample-box 4)
(possible-moves sample-box 3)

;;Task 6
(defn play-game [box]
  (if (win? box) :win
    (let [roll (dice-roll)
          moves (possible-moves box roll)]
      (if (empty? moves) :loss
        (recur (first moves))))))
(play-game (disj start-box 1))
(play-game easy-box)

;;Task 7
(defn simulate [num-trials]
  (loop [n num-trials
         record {:win 0, :loss 0}]
    (if (zero? n)
      (double (/ (:win record) (+ (:win record) (:loss record))))
      (recur (dec n) (update-in record [(play-game (disj start-box 1))] inc)))))
(simulate 1000000)
;Probability of winning the simplified game is about 0.02%


