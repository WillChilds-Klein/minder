(ns minder.core
  (:require
    [clojure.data.json :as json]
    [clj-time.core :as t]
    [clj-time.format :as f]))

(def factbase-url "https://factba.se/rss/calendar.json")
(def est-parser (f/formatter :date-hour-minute-second
                             (t/time-zone-for-id "America/New_York")))
(def iftt-parser (f/formatter "MMMM dd, yyyy 'at' hh:mmaa"
                              (t/time-zone-for-id "America/New_York")))

(defn fetch-schedule
  "cache json url's data on local filesystem and return that data. returns
   cached data if network is unavailable"
  ([url] (fetch-schedule url "/tmp/trumpMinderCalendarCache.txt"))
  ([url local-path]
   (try
     (let [data (slurp url)
           data (json/read-json data)]
       (spit local-path (json/json-str data)) ; cache results for offline dev NOTE: MAY NOT WORK ON LAMBDA
       data)
     (catch Exception e
       (println "WARNING: caught exception while querying schedule, falling back
                 to cached data")
       (json/read-json (slurp local-path))))))

(defn parse-date
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
  (let [fmtr (f/formatter :date-hour-minute-second)]
    (reduce #(assoc %1 (f/unparse fmtr (parse-date %2)) %2)
      {}
      data)))

(defn datetime-is-between?
  [datetime d1 d2]
  (t/within? (t/interval (f/parse d1) (f/parse d2))
             datetime))

(defn walk-dates
  [target dates]
  (cond
    (< (count dates) 2) '()
    (datetime-is-between? target (first dates) (second dates)) (first dates)
    :else (walk-dates target (pop dates))))

(defn find-current-event
  [current-datetime date-map]
  (->> date-map
       keys
       (sort #(compare %2 %1))
       (into '())
       (walk-dates current-datetime)
       date-map))

(defn current-event
  ([] (current-event (t/now)))
  ([d] (->> factbase-url
            fetch-schedule
            construct-date-map
            (find-current-event d))))

; ----------------------------------------

(defn- abbrv-text
  [text]
  (if (> (count text) 230)
    (-> text (subs 0 230) (str "..."))
    text))

(defn- salt-text
  [text]
  (apply str text " " (take 3 (repeatedly #(char (+ (rand 26) 65))))))

(defn compose-tweet
  [data]
  (let [dtime (f/parse iftt-parser (:created data))
        event (current-event dtime)]
    (when-not (nil? event)
        (->> event
             :details
             abbrv-text
             salt-text
             (format "the commander in queef is tweeting during \"%s\"")))))
