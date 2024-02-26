(ns hkit-test.core
  (:require [org.httpkit.server :as server]
            [ring.middleware.refresh :as refresh]))


; Until https://github.com/weavejester/ring-refresh/pull/16 is merged
(defn wrap-refresh
  [handler]
  (let [wrap-success (fn [h]
                       (fn [req]
                         (with-redefs [refresh/success? (constantly true)]
                           (h req))))]
    (-> handler
        (refresh/wrap-refresh)
        (wrap-success))))

(defn html-link
  [destination-int]
  (str "<!DOCTYPE html>\n<html>\n<head>\n    <title>Minimal HTML with Link</title>\n</head>\n<body>\n    <a href=\""
       (str "/" destination-int)
       "\">"
       (str "Link to "destination-int)
       "</a>\n</body>\n</html>\n"))

(defn base-handler
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body   (case (:uri req)
           "/" (html-link 1)
           "/1" (html-link 2)
           "/2" (html-link 3)
           "/3" (html-link 4)
           "/4" (html-link 5)
           (html-link nil))})

(def handler
  (wrap-refresh base-handler))

(defonce *server (atom nil))

(defn start! []
  (if-not @*server
    (reset! *server (server/run-server #'handler
                                       {:port 3000
                                        :legacy-return-value? false}))
    (throw (ex-info "Server already started" {}))))

(defn stop! []
  (when-let [server @*server]
    @(server/server-stop! server))
  (reset! *server nil))
