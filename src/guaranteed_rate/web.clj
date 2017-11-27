(ns guaranteed-rate.web
  (:require [guaranteed-rate.api :as api]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response status]]
            [ring.middleware.json :refer [wrap-json-response]]))

(defn post-record [line]
  (let [result (api/process-record line)
        resp-code (if (:error result) 400 200)]
    (-> result
        (response)
        (status resp-code))))

(defroutes handler
  (context "/records" []
    (POST "/" {:keys [body]} (-> (slurp body) (post-record)))
    (GET "/gender" []
      (response (api/get-processed-records :gender-lname)))
    (GET "/birthdate" []
      (response (api/get-processed-records :birthdate)))
    (GET "/name" []
      (response (api/get-processed-records :lastname)))) 
  (route/not-found "No matching route found for request"))

(def app (wrap-json-response handler))

(defn start-web []
  (run-jetty app {:port 3000}))
