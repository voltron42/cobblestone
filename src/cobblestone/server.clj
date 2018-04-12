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
            [cheshire.core :as json]
            [cobblestone.json :refer [j2e]])
  (:gen-class))

(defn- build-svg [code]
  (let [docs (j2e (json/parse-string code keyword))
        svgs (cob/build-svgs-from-tile-docs docs)]
    (vals svgs)))

(defn- build-img [code]
  (let [docs (j2e (json/parse-string code keyword))
        svgs (cob/build-svgs-from-tile-docs docs)]
    (map cob/svg-to-64 (vals svgs))))

(defn- build-app []
  (sweet/routes
    (sweet/api {}
               (api/context "/svg" []
                            (sweet/resource {:description ""
                                             :post        {:summary    ""
                                                           :parameters {:body s/Any}
                                                           :responses  {200 {:schema s/Any}}
                                                           :handler    (fn [{body :body}]
                                                                         (let [code (slurp body)]
                                                                           (http/content-type
                                                                             (http/ok (build-svg code))
                                                                             "application/json")))}}))
               (api/context "/img" []
                            (sweet/resource {:description ""
                                             :post        {:summary    ""
                                                           :parameters {:body s/Any}
                                                           :responses  {200 {:schema s/Any}}
                                                           :handler    (fn [{body :body}]
                                                                         (let [code (slurp body)]
                                                                           (http/content-type
                                                                             (http/ok (build-img code))
                                                                             "application/json")))}}))
               (sweet/GET "/" [] (resp/redirect "/index.html")))
    (route/resources "/")
    (route/not-found "404 Not Found")))

(defn -main [& [port]]
  (let [my-app (build-app)
        port (Integer. ^int (or port (env :port) 5000))]
    (server/run-server my-app {:port port :join? false})))