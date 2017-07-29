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
;(tree-simulation start-box 1000)

(def all-boxes
  (map (partial into #{}) (power (range 1 10))))
(count all-boxes)
(def look-up-table
  (zipmap all-boxes (map (fn [x] (tree-simulation x 100)) all-boxes)))

;;Save and load the table
;(spit "shutthebox-table.txt" (zipmap (map (fn [x] (apply sorted-set x)) (keys look-up-table)) (vals look-up-table)))
;(def look-up-table (read-string (slurp "shutthebox-10000000.txt")))


;;Task 11: Monte Carlo tree search

(defn ucb [record]
  (if (nil? record)
    (Double/POSITIVE_INFINITY)
    (let [w (get record :win 0)
          l (get record :loss 0)
          n (+ w l)]
      (if (zero? w)
        (double (/ 3 n))
        (/ (+ w (* 2 (Math/sqrt (/ (* w l) n)))) n)))))
(ucb {:win 5, :loss 5})
(ucb {:win 50, :loss 50})
(ucb {:win 0, :loss 5})
(ucb {:win 1, :loss 5})
(ucb {:win 0, :loss 50})
(ucb {:win 1, :loss 50})
(ucb {:win 1, :loss 0})
(ucb {:win 9, :loss 0})
(ucb {:win 9, :loss 1})
(ucb nil)

(defn find-best-child [tree children]
  (let [child-values (map (comp ucb tree) children)]
    (key (apply max-key val (zipmap children child-values)))))
(find-best-child
  {#{2 3 5} {:win 1, :loss 1}
   #{2 3} {:win 0, :loss 2}
   #{5} {:win 1, :loss 1}}
  #{#{2 3} #{5}})
(find-best-child
  {#{2 3 5} {:win 1, :loss 1}
   #{2 3} {:win 0, :loss 2}}
  #{#{2 3} #{5}})
(find-best-child
  {#{2 3 5} {:win 1, :loss 1}} #{#{2 3} #{5}})

(defn add-result [tree box result]
  [(assoc tree box
     (merge-with + (get tree box) (assoc {} result 1))) result])

;;Returns the updated tree and the most recent result in a vector
(defn uct [tree box]
  (if-let [record (get tree box)]
    (let
      [roll (dice-roll)
       children (possible-moves box roll)]
      (cond
        (empty? children)
        (add-result tree box :loss)
        (empty? (first children))
        (add-result tree box :win)
        :else
        (let [best-child (find-best-child tree children)
              [updated-tree new-result] (uct tree best-child)]
          (add-result updated-tree box new-result))))
    (let [new-result (play-game box)]
      [(assoc-in tree [box new-result] 1) new-result])))
(uct {} easy-box)
(uct {} start-box)
(uct {start-box {:win 0, :loss 1}} start-box)

(defn monte-carlo-tree-search [box num-times]
  (let [record
        ((loop [n num-times tree {}]
           (if (zero? n) tree
             (recur (dec n) (first (uct tree box))))) box)]
         (double (/ (:win record) (+ (:win record) (:loss record))))))
;(monte-carlo-tree-search start-box 100000)
;For 1000 dives into the tree, the probability of winning is about 0.0328.
;For 10000, the probability is about 0.0442.
;Learning has occurred! We have achieved AI!


;;Task 12: Construct the full game tree

;;Probabilties for a pair of dice
(def dice-probs
  (into {}
    (map
      (fn [[k v]] [k (/ v 36)])
      (frequencies
        (for [x (range 1 7) y (range 1 7)] (+ x y))))))

;;Construct the table of probabilties
(defn build-tree [tree [box roll]]
  (if (get tree [box roll]) tree
    (let
      [children
       (cond
         (empty? box) '()
         roll  (map (fn [b] [b nil]) (possible-moves box roll))
         :else (map (fn [r] [box r]) (keys dice-probs)))
       tree-with-children
       (reduce build-tree tree children)
       child-probs
       (map tree-with-children children)]
      (assoc tree-with-children [box roll]
        (cond
          (empty? box) 1
          (empty? children) 0
          roll  (reduce max child-probs)
          :else (reduce + (map * child-probs (vals dice-probs))))))))
(def full-tree (build-tree {} [start-box nil]))
(double (full-tree [start-box nil]))
;;The table is missing one value because the state is impossible to reach.
(double ((build-tree full-tree [(disj start-box 1) nil]) [(disj start-box 1) nil]))

(def look-up-table-optimal
  (zipmap
    (map (fn [box] (apply sorted-set box)) all-boxes)
    (map (fn [box] (if-let [p (full-tree [box nil])] (double p) nil)) all-boxes)))

;;Random play
(tree-simulation start-box 100000)
;;Upper confidence bound learning
(monte-carlo-tree-search start-box 100000)
;;Optimal play
(look-up-table-optimal start-box)


;;Task 9: Let the user play the game

(defn print-state [box roll]
  (println (str "Current box: " (apply sorted-set box) " roll: " roll)))

(defn print-choice [i box]
  (println
    (str " " (inc i) ": "
         (apply sorted-set box) " ("
         (look-up-table-optimal box) ")")))

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

