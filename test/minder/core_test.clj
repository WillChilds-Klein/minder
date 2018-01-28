(ns minder.core-test
  (:require [clojure.test :refer :all]
            [minder.core :refer :all]))

(deftest a-failing-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(deftest a-successful-test
  (testing "no need to fix here!"
    (is (= (+ 1 1) 2))))
