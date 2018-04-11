(ns cobblestone.server
  (:require [compojure.api.sweet :as sweet]
            [compojure.api.core :as api]
            [ring.util.http-response :as http]
            [ring.util.response :as resp]
            [compojure.route :as route]
            [org.httpkit.server :as server]
            [environ.core :refer [env]]
            [schema.core :as s]
            [cobblestone.core :as cob]
            [clojure.pprint :as pp]
            [clojure.edn :as edn])
  (:gen-class))

(defn- build-response [code]
  (let [docs (edn/read-string code)
        svgs (cob/build-svgs-from-tile-docs docs)]
    (vals svgs)))


(defn- build-app []
  (sweet/routes
    (sweet/api {}
               (api/context "/build" []
                            (sweet/resource {:description ""
                                             :post        {:summary    ""
                                                           :parameters {:body s/Any}
                                                           :responses  {200 {:schema s/Any}}
                                                           :handler    (fn [{body :body}]
                                                                         (let [code (slurp body)]
                                                                           (http/content-type
                                                                             (http/ok (build-response code))
                                                                             "application/json")))}}))
               (sweet/GET "/" [] (resp/redirect "/index.html")))
    (route/resources "/")
    (route/not-found "404 Not Found")))

(defn -main [& [port]]
  (let [my-app (build-app)
        port (Integer. ^int (or port (env :port) 5000))]
    (server/run-server my-app {:port port :join? false})))