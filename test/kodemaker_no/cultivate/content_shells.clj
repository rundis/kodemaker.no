(ns kodemaker-no.cultivate.content-shells)

(defn content [m]
  (merge {:people {}
          :tech {}
          :projects {}
          :articles {}}
         m))

(defn tech [m]
  (merge {:name "!"
          :description "!"}
         m))

(defn person [m]
  (merge {:name ["!"]
          :title "!"
          :start-date "!"
          :description "!"
          :phone-number "!"
          :email-address "!"
          :presence {}}
         m))

(defn recommendation [m]
  (merge {:link {:url "!" :text "!"}
          :title "!"
          :blurb "!"
          :tech []}
         m))