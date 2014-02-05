(ns kodemaker-no.cultivate.blog-posts
  (:require [clj-time.format :as format]
            [clojure.string :as str]
            [kodemaker-no.homeless :refer [update-in-existing]]))

(defn to-date [str]
  (format/parse (format/formatters :year-month-day) str))

(defn blog-post-path [path]
  (str "/blogg" (str/replace path #"\.md$" "/")))

(defn load-blog-post [path blog-post]
  (-> blog-post
      (update-in-existing [:published] to-date)
      (assoc :path (blog-post-path path))))

(defn cultivate-blog-posts [blog-posts]
  (into {} (map (fn [[path blog-post]]
                  [path (load-blog-post path blog-post)]) blog-posts)))