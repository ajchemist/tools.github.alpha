(ns user.tools.github.alpha
  (:require
   [clj-http.client :as http]
   [caesium.crypto.box :as box]
   )
  (:import
   java.util.Base64
   ))


(set! *warn-on-reflection* true)


(def ^:dynamic *print* true)


;;


(defn decode-base64
  ^bytes
  [^String encoded]
  (.decode (Base64/getDecoder) encoded))


(defn encode-base64
  ^bytes
  [^bytes s]
  (.encode (Base64/getEncoder) s))


;;


(defn wrap-meta-response
  [handler]
  (letfn [(f [{:keys [body] :as response}]
            (if (instance? clojure.lang.IMeta body)
              (with-meta body (dissoc response :body))
              response))]
    (fn
      ([request]
       (f (handler request)))
      ([request respond raise]
       (handler request #(respond (f %)) raise)))))


(def ^{:arglists '([request] [request respond raise])}
  request
  (-> http/request
    (wrap-meta-response)))


(defn client
  [{:keys [url] :as req}]
  (try
    (request req)
    (catch Exception e
      (cond
        ;; (re-find #"Bad Request: message is not modified" (:body (ex-data e)))
        ;; (println "Github request failed:" url (pr-str req))

        :else
        (do
          (println "Github request failed:" url (pr-str req))
          (throw e))))))


;;


(defn list-user-repos
  [{:keys [:github/username] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/users/" username "/repos")
      :method :get
      :as :json-strict-string-keys
      )))


(defn actions-list-repo-secrets
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      )))


(defn actions-get-repo-pub-key
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets/public-key")
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      )))


(defn actions-get-repo-secret
  [{:keys
    [:github/owner
     :github/repo
     :github.actions/secret-name] :as req}]
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets/" secret-name)
      :method :get
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json")))


(defn actions-put-repo-secret
  [{:keys
    [github/owner
     github/repo
     github.actions.secrets/secret-name
     ^String github.actions.secrets/secret-value
     github.actions.secrets/public-key
     github.actions.secrets/public-key-id] :as req}]
  (when *print* (println "Create or update actions secret:" secret-name))
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/actions/secrets/" secret-name)
      :method :put
      :as :json-strict-string-keys
      :accept "application/vnd.github.v3+json"
      :content-type :application/json
      :form-params (cond-> {:encrypted_value
                            (String.
                              (encode-base64
                                (box/box-seal (.getBytes secret-value) (decode-base64 public-key))))}
                     public-key-id (assoc :key_id public-key-id)))))


(defn create-repo
  [req]
  {:pre [(string? (get-in req [:form-params "name"]))]}
  (when *print* (println "Creating repository:" (get-in req [:form-params "name"])))
  (client
    (assoc req
      :url          "https://api.github.com/user/repos"
      :method       :post
      :content-type :json
      :as           :json-string-keys)))


(defn delete-repo
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url          (str "https://api.github.com/repos/" owner "/" repo)
      :method       :delete
      :content-type :json
      :as           :json-string-keys)))


(defn list-deploy-keys
  [{:keys [:github/owner :github/repo] :as req}]
  (client
    (assoc req
      :url    (str "https://api.github.com/repos/" owner "/" repo "/keys")
      :method :get
      :as     :json-string-keys)))


(defn post-deploy-key
  [{:keys [:github/owner :github/repo] :as req}]
  (println "Deploying key:" (str owner "/" repo))
  (client
    (assoc req
      :url (str "https://api.github.com/repos/" owner "/" repo "/keys")
      :method :post
      :content-type :json
      :as :json-string-keys)))


(set! *warn-on-reflection* false)
