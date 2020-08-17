(ns forecasts.core-test
  (:require [forecasts.core :as sut]
            [clojure.test :refer [are is testing deftest]]
            [clojure.string :as cstr]))

(def forecast-example
  {:currently {:summary            "Drizzle"
               :precip-probability 0.9}
   :hourly    {:summary "Rain."}
   :daily     {:data [{:icon "rain"}
                      {:icon "rain"}
                      {:icon "partly-cloudy-day"}]}})

(deftest summary-report
  (let [report (sut/get-summary-report forecast-example)]
    (testing "target keys are in report"
      (are [x y] (cstr/includes? x y)
        report (get-in forecast-example [:currently :summary])
        report (str (get-in forecast-example [:currently :precip-probability]))
        report (get-in forecast-example [:hourly :summary])))))

(deftest daily-icons-report
  (let [report             (sut/get-daily-icons-report forecast-example)
        expected-icons-str "2 days rain, 1 days partly-cloudy-day"]
    (is (cstr/includes? report expected-icons-str))))
