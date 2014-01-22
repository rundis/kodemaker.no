(ns kodemaker-no.cultivate.people
  (:require [clojure.string :as str]
            [kodemaker-no.homeless :refer [update-vals]]
            [kodemaker-no.cultivate.tech :as tech]))

(defn- add-str [person]
  (assoc person :str (-> person :id str (subs 1))))

(defn- add-url [person]
  (assoc person :url (str "/" (:str person) "/")))

(defn- fix-names [person]
  (-> person
      (assoc :full-name (str/join " " (:name person)))
      (assoc :first-name (first (:name person)))))

(defn- add-genitive [person]
  (assoc person :genitive
         (str (:first-name person)
              (if (.endsWith (:first-name person) "s")
                "'"
                "s"))))

(defn- add-photos [person]
  (assoc person :photos
         {:side-profile (str "/photos/people/" (:str person) "/side-profile.jpg")
          :half-figure (str "/photos/people/" (:str person) "/half-figure.jpg")}))

(defn- look-up-tech [content person]
  (-> person
      (update-in [:tech :favorites-at-the-moment] #(tech/look-up-tech content %))
      (update-in [:tech :want-to-learn-more] #(tech/look-up-tech content %))))

(defn- cultivate-person [content person]
  (->> person
       add-str
       add-url
       fix-names
       add-genitive
       add-photos
       (look-up-tech content)))

(defn cultivate-people [content]
  (update-in content [:people] #(update-vals % (partial cultivate-person content))))
