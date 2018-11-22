(ns minder.core
  (:require
    [clojure.data.json :as json]
    [clj-time.core :as t]
    [clj-time.format :as f]))

(def factbase-url "https://factba.se/rss/calendar.json")
(def est (t/time-zone-for-id "America/New_York"))
(def est-parser (f/formatter :date-hour-minute-second est))
(def iftt-parser (f/formatter "MMMM dd, yyyy 'at' hh:mmaa" est))

(defn fetch-schedule
  "cache json url's data on local filesystem and return that data. returns
   cached data if network is unavailable"
  ([url] (fetch-schedule url "/tmp/trumpMinderCalendarCache.txt"))
  ([url local-path]
   (try
     (let [data (slurp url)
           data (json/read-json data)]
       (spit local-path (json/json-str data))
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
  (let [unfmtr (f/formatter :date-hour-minute-second)]
    (reduce #(assoc %1 (f/unparse unfmtr (parse-date %2)) %2)
      {}
      data)))

(defn datetime-is-between?
  [datetime d1 d2]
  (let [intvl (t/interval (f/parse d1) (f/parse d2))]
    (and (t/within? intvl datetime)
         (< (t/in-minutes intvl) 90)))) ; 90 min. timeout

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
  (if (> (count text) 220)
    (-> text
        (subs 0 220)
        (str "..."))
    text))

(defn- append-time
  [dtime text]
  (str text " at " (f/unparse (f/formatter :hour-minute est) dtime) "."))

(defn compose-tweet
  [data]
  (when-not (empty? data)
    (println "data:" data)
    (let [dtime (f/parse iftt-parser (:created data))
          event (current-event dtime)]
      (when-not (nil? event)
        (println "event:" event)
        (->> event
             :details
             abbrv-text
             (format "the cheeto-in-chief tweeted during \"%s\"")
             (append-time dtime))))))

;; TODO
;; ====
;; - instead of constructing a 'date-map keyed by date, sort list of
;;   calendar events lexically by (f/unparse unfmtr (parse-date event))
;; - filter events by :type tag?
;; - stretch: use binary search instead of linear in 'walk-dates
