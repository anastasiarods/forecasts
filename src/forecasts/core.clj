(ns forecasts.core
  (:require [hato.client :as client]
            [clojure.data.json :as json]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as cstr]
            [java-time :as time]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def darksky-api-key (System/getenv "DARKSKY_API_KEY"))
(def forecasts-url (partial format "https://api.darksky.net/forecast/%s/%s,%s"))
(def time-machine-request-url (partial format "https://api.darksky.net/forecast/%s/%s,%s,%s"))
(def cities-url "https://raw.githubusercontent.com/lutangar/cities.json/master/cities.json")

(def summary-template (partial format "Current weather - %s, Today we will see - %s with a %s%% chance of rain."))
(def hottest-day-template (partial format "This week the hottest day will be %s."))
(def icon-frequencies-template (partial format "This week we should have %s."))

(def temperature-max-key :temperature-max)

(defn get-forecast
  "returns the current weather forecast for the next week."
  [{:keys [lat lng]}]
  (when darksky-api-key
    (-> (forecasts-url darksky-api-key lat lng)
        client/get
        :body
        (json/read-str :key-fn csk/->kebab-case-keyword))))

(defn get-time-machine-forecast
  "returns the observed or forecast weather conditions for a date in the past or future."
  [{:keys [lat lng time]}]
  (-> (client/get (time-machine-request-url darksky-api-key lat lng time))
      :body
      (json/read-str :key-fn csk/->kebab-case-keyword)))

(defn get-cities-list
  "returns list of {:contry :name :lat :lng }"
  []
  (-> (client/get cities-url)
      :body
      (json/read-str :key-fn csk/->kebab-case-keyword)))

(defn equal-strings? [a b]
  (= (cstr/lower-case a) (cstr/lower-case b)))

(defn find-city-coordinates [city-name]
  (->> (get-cities-list)
       (filter #(equal-strings? city-name (:name %)))
       first))

(defn find-value-with-max-key [coll key]
  (last (sort-by key coll)))

(defn get-day-of-week [epoch-in-sec zone]
  (time/day-of-week
   (time/as
    (time/offset-date-time (time/instant (* epoch-in-sec 1000)) zone)
    :day-of-week)))

(defn extract-summary-fields [resp]
  {:currently-summary            (get-in resp [:currently :summary])
   :hourly-summary               (get-in resp [:hourly :summary])
   :currently-precip-probability (get-in resp [:currently :precip-probability])})

(defn get-summary-report [forecast]
  (let [{:keys [currently-summary hourly-summary currently-precip-probability]}
        (extract-summary-fields forecast)]
    (summary-template currently-summary
                      hourly-summary
                      currently-precip-probability)))

(defn get-hottest-day-report [forecast]
  (let [daily-forecast (get-in forecast [:daily :data])
        hottest-day    (find-value-with-max-key daily-forecast temperature-max-key)
        day-of-week    (get-day-of-week (:time hottest-day) (:timezone forecast))]
    (hottest-day-template (cstr/capitalize day-of-week))))

(defn get-daily-icons-report [forecast]
  (let [daily-forecast (get-in forecast [:daily :data])
        icons          (frequencies (map :icon daily-forecast))
        icons-str      (map #(str (second %) " days " (first %)) icons)]
    (icon-frequencies-template (cstr/join ", " icons-str))))

(defn do-reports [coordinates]
  (let [forecast    (get-forecast coordinates)
        summary     (get-summary-report forecast)
        hottest-day (get-hottest-day-report forecast)
        daily-icons (get-daily-icons-report forecast)]
    (println summary)
    (println hottest-day)
    (println daily-icons)))

(def cli-options
  [[nil "--city CITY" "City name"
    :parse-fn identity]
   [nil "--lat LAT" "Latitude"
    :default 60.59329987
    :parse-fn read-string]
   [nil "--lng LNG" "Longitude"
    :default -1.44250533
    :parse-fn read-string]
   ["-h" "--help"]])

(defn -main
  [& args]
  (let [{:keys [city lat lng]}
        (:options (cli/parse-opts args cli-options))

        coordinates (if city
                      (find-city-coordinates city)
                      {:lat lat :lng lng})]
    (if (and city (nil? coordinates))
      (println "No results found for " city)
      (do-reports coordinates))))
