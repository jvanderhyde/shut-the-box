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

;;Task 8
(defn power [s]
  (loop [[f & r] (seq s) p '(())]
    (if f (recur r (concat p (map (partial cons f) p))) p)))
(def lever-power (power [1 2 3 4 5 6 7 8 9]))
(defn levers-for-roll [roll]
  (filter (fn [coll] (= roll (reduce + coll)))
    lever-power))
(levers-for-roll 9)
(def lever-combinations (into [] (map levers-for-roll (range 13))))
(lever-combinations 9)

;;Task 4 and 8
(defn possible-moves [box roll]
  (map (fn [move] (reduce disj box move))
      (filter (partial every? box) (lever-combinations roll))))
(possible-moves sample-box 9)
(possible-moves sample-box 3)

;;Task 6
(defn play-game [box]
  (if (win? box) :win
    (let [roll (dice-roll)
          moves (possible-moves box roll)]
      (if (empty? moves) :loss
        (recur (rand-nth (into [] moves)))))))
(play-game start-box)
(play-game easy-box)

;;Task 7
(defn simulate [num-trials]
  (loop [n num-trials
         record {:win 0, :loss 0}]
    (if (zero? n)
      (double (/ (:win record) (+ (:win record) (:loss record))))
      (recur (dec n) (update-in record [(play-game start-box)] inc)))))
;(simulate 1000000)
;Probability of winning the simplified game is about 0.02%
;Probability of winning the real game is about 2%, assuming random play.


;;Task 10: Look-up table

(defn tree-simulation-record [initial-box num-times]
  (loop [n num-times
         record {:win 0, :loss 0}]
    (if (zero? n) record
      (recur
        (dec n)
        (update-in record [(play-game initial-box)] inc)))))

(defn tree-simulation [initial-box num-times]
  (let [record (tree-simulation-record initial-box num-times)]
    (double (/ (get record :win) num-times))))
(tree-simulation start-box 1000)

(def all-boxes
  (map (partial into #{}) (power (range 1 10))))
(count all-boxes)
(def look-up-table
  (zipmap all-boxes (map (fn [x] (tree-simulation x 100)) all-boxes)))

;;Save and load the table
;(spit "shutthebox-table.txt" (zipmap (map (fn [x] (apply sorted-set x)) (keys look-up-table)) (vals look-up-table)))
;(def look-up-table (read-string (slurp "shutthebox-10000000.txt")))


;;Task 9: Let the user play the game

(defn print-state [box roll]
  (println (str "Current box: " (apply sorted-set box) " roll: " roll)))

(defn print-choice [i box]
  (println
    (str " " (inc i) ": "
         (apply sorted-set box) " ("
         (look-up-table box) ")")))

(defn user-choose [box roll moves]
  (println "Possible moves:")
  (doall (map-indexed print-choice moves))
  (print " Choice? ") (flush)
  (nth (vec moves) (dec (read))))

(defn play-game-user [box]
  (if (empty? box) :win
    (let [roll (dice-roll)
          moves (possible-moves box roll)]
      (print-state box roll)
      (if (empty? moves) :loss
          (recur (user-choose box roll moves))))))
;(play-game-user start-box)

