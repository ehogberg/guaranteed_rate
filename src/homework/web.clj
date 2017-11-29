(ns homework.web
  (:require [compojure.core :refer [defroutes context GET POST]]
            [compojure.route :refer [not-found]]
            [homework.api :refer [process-record get-processed-records]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response status]]))

(defn post-record [line]
  (let [result (process-record line)
        resp-code (if (:error result) 400 200)]
    (-> result
        (response)
        (status resp-code))))

(defroutes handler
  (context "/records" []
    (POST "/" {:keys [body]} (-> (slurp body) (post-record)))
    (GET "/gender" []
      (response (get-processed-records :gender-lname)))
    (GET "/birthdate" []
      (response (get-processed-records :birthdate)))
    (GET "/name" []
      (response (get-processed-records :lastname)))) 
  (not-found "No matching route found for request"))

(def app (wrap-json-response handler))

(defn start-web []
  (run-jetty app {:port 3000}))
