(ns minder.twitter
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful]))

(def consumer-key (System/getenv "TWITTER_CONSUMER_KEY"))
(def consumer-sec (System/getenv "TWITTER_CONSUMER_SECRET"))
(def user-key (System/getenv "TWITTER_USER_KEY"))
(def user-secret (System/getenv "TWITTER_USER_SECRET"))

(def my-creds (make-oauth-creds consumer-key consumer-sec user-key user-secret))

(defn post-tweet
  [s]
  (when-not (nil? s)
    (statuses-update :oauth-creds my-creds
                     :params {:status s})
    "status updated"))