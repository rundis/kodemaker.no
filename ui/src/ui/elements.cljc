(ns ui.elements
  (:require [ui.typography :as t]))

(defn arrow [{:keys [width]}]
  [:div.arrow [:div.arrow-head]])

(def size-classes
  {:large "text-l"})

(defn arrow-link [{:keys [text href size] :as params}]
  [:a.ib {:href href
          :className (size-classes size)}
   text (arrow params)])

(defn icon [{:keys [type width height]}]
  [:svg {:view-box "0 0 24 24"
         :width width
         :height height}
   [:use {:xlink-href (str "/icons/" (namespace type) "/" (name type) ".svg#icon")
          :style {"--svg_color" "var(--rouge)"}}]])

(defn seymour [{:keys [icon title text link]}]
  [:div.seymour
   [:div.seymour-top
    (icon icon)
    (t/h4 {} title)
    [:p text]]
   [:div.seymour-bottom
    (arrow-link link)]])
