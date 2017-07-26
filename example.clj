;; Monte Carlo simulation
;; Created by James Vanderhyde, 22 September 2016

;; Define how to play the game:
;;  Flip a coin.
(defn flip-coin []
  (if (zero? (rand-int 2))
    :heads
    :tails))

;; Play one game
(def one-trial (flip-coin))
one-trial

;; Define what makes a winning game:
;;  Heads you win.
(defn win? [game-result]
  (= :heads game-result))

;; Test the win? function
(win? one-trial)

;; Play several games
(def games
  (repeatedly 100 flip-coin))

;; Take a look at the outcomes
games

;; Count how many were wins
(count (filter win? games))

;; Calculate the probability of winning
;;  (number of successes divided by number of trials)
(def probability-of-winning
  (double
    (/ (count (filter win? games))
       (count games))))
probability-of-winning
