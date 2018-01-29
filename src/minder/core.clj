(ns minder.core
  (:require
    [clojure.data.json :as json]
    [clojure.pprint :as pprint]
    [clj-time.core :as t]
    [clj-time.format :as f]))

(def factbase-url "https://factba.se/rss/calendar.json")
(def est-parser (f/formatter :date-hour-minute-second
                             (t/time-zone-for-id "America/New_York")))

(defn fetch-schedule
  "cache json url's data on local filesystem and return that data. returns
   cached data if network is unavailable"
  ([url] (fetch-schedule url "/tmp/trumpMinderCalendarCache.txt"))
  ([url local-path]
   (try
     (let [data (slurp url)
           data (json/read-json data)]
       (spit local-path (json/json-str data)) ; cache results for offline dev
       data)
     (catch Exception e
       (println "WARNING: caught exception while querying schedule, falling back
                 to cached data")
       (json/read-json (slurp local-path))))))

(defn parse-date-from-datum
  "given a single event object, returns its start time as a DateTime"
  [datum]
  (t/to-time-zone (f/parse est-parser
                           (str (if (nil? (:date datum))
                                  "1970-01-01"
                                  (:date datum))
                                "T"
                                (if (nil? (:time datum))
                                  "00:00:00"
                                  (:time datum))))
                  (t/time-zone-for-id "UTC")))

(defn construct-date-map
  "constructs a map keyed by UTC start time str of event"
  [data]
  (reduce (fn [acc v] (assoc acc (f/unparse (f/formatter :date-hour-minute-second)
                                            (parse-date-from-datum v)) v))
          {}
          data))

(defn now-is-between?
  [d1 d2]
  (t/within? (t/interval (f/parse d1) (f/parse d2))
             (t/now)))

(defn walk-dates
  [dates]
  (cond
    (< (count dates) 2) '()
    (now-is-between? (first dates) (second dates)) (first dates)
    :else (recur (pop dates))))

(defn find-current-event
  [date-map]
  (let [ks (keys date-map)
        ks (sort #(compare %2 %1) ks) ; sort in reverse lexicographic order
        ks (into '() ks)
        date-key (walk-dates ks)]
    (get date-map date-key)))

(defn current-event
  []
  (->> factbase-url
       fetch-schedule
       construct-date-map
       find-current-event))

(defn- abbrv-tweet
  [text]
  (if (> (count text) 240 )
    (-> text (subs 0 240) (str "..."))
    text))

(defn compose-tweet
  [data]
  (->> data
       :text
       abbrv-tweet
       (format "the commander in queef says: \"%s\"")))

(pprint/pprint (current-event))
