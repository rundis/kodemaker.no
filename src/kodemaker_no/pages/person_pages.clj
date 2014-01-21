(ns kodemaker-no.pages.person-pages
  (:require [kodemaker-no.formatting :refer [to-html]]
            [hiccup.core :as hiccup]
            [clojure.string :as str]))

(defn- render-recommendation [rec]
  (list [:h3 [:a {:href (:url rec)} (:title rec)]]
        [:p (:blurb rec)]))

(defn- render-recommendations [person recs]
  (list [:h2 (str (:genitive person) " Anbefalinger")]
        (map render-recommendation recs)))

(defn into-paragraph [html node]
  (str/replace html #"^<p>" (str "<p>" (hiccup/html node))))

(defn- render-hobby [hobby]
  [:div.bd
   [:h3.mtn (:title hobby)]
   (into-paragraph (to-html :md (:description hobby))
                   [:img.right {:src (:illustration hobby)}])])

(defn- render-hobbies [person hobbies]
  (list [:h2 "Snakker gjerne om"]
        (map render-hobby hobbies)))

(defn- person-page [person]
  {:title (:full-name person)
   :illustration (-> person :photos :half-figure)
   :lead [:p (:description person)]
   :aside [:div.tight
           [:h4 (:full-name person)]
           [:p
            (:title person) "<br>"
            [:span.nowrap (:phone-number person)] "<br>"
            [:a {:href (str "mailto:" (:email-address person))}
             (:email-address person)]]]
   :body (list
          (when-let [xs (:recommendations person)]
            (render-recommendations person xs))
          (when-let [xs (:hobbies person)]
            (render-hobbies person xs)))})

(defn person-pages [people]
  (into {} (map (juxt :url #(partial person-page %)) people)))
