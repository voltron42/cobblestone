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
            [clojure.pprint :as pp])
  (:import (java.io ByteArrayInputStream))
  (:gen-class))

(defn- build-html [code]
  (let [html-text (cob/build-html-from-tile-docs-text code)]
    (pp/pprint html-text)
    html-text))


(defn- build-app []
  (sweet/routes
    (sweet/api {}
               (api/context "/build" []
                            (sweet/resource {:description ""
                                             :post        {:summary    ""
                                                           :parameters {:body s/Any}
                                                           :responses  {200 {:schema s/Str}}
                                                           :handler    (fn [{body :body}]
                                                                         (let [code (slurp body)]
                                                                           (http/content-type
                                                                             (http/ok (build-html code))
                                                                             "text/html")
                                                                           ))}}))
               (sweet/GET "/" [] (resp/redirect "/index.html")))
    (route/resources "/")
    (route/not-found "404 Not Found")))

(defn -main [& [port]]
  (let [my-app (build-app)
        port (Integer. ^int (or port (env :port) 5000))]
    (server/run-server my-app {:port port :join? false})))