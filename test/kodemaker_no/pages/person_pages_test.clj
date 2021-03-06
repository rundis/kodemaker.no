(ns kodemaker-no.pages.person-pages-test
  (:require [clj-time.core :as time]
            [clojure.string :as str]
            [hiccup.core :refer [html]]
            [kodemaker-no.pages.person-pages :refer :all]
            [midje.sweet :refer :all]))

(def magnar
  {:url "/magnar/"
   :full-name "Magnar Sveen"
   :genitive "Magnars"
   :title "Framsieutvikler"
   :photos {:half-figure "/photos/magnars/half-figure.jpg"}
   :description "The *description*"
   :phone-number "+47 918 56 425"
   :email-address "magnar@kodemaker.no"
   :next-person-url "/anders/"})

(defn remove-flexmark-quirky-newlines [s]
  (str/replace s "</p>\n" "</p>"))

(defn page [& {:as extras}]
  (((person-pages [(merge magnar extras)]) "/magnar/")))

(defn page-at [date & {:as extras}]
  (((person-pages [(merge magnar extras)] date) "/magnar/")))

(fact (-> (page) :title) => {:h1 "Magnar Sveen"
                             :head "Magnar Sveen"
                             :arrow "/anders/"})

(fact (-> (page) :lead) => "<p>The <em>description</em></p>\n")

(fact (-> (page) :aside html remove-flexmark-quirky-newlines)
      => (html [:div.illustration.mbn
                [:img {:src "/photos/magnars/half-figure.jpg"}]]
               [:div.tight
                [:hr.mtn.s-3of4]
                [:h4 "Magnar Sveen"]
                [:p
                 "Framsieutvikler" "<br>"
                 [:span.nowrap "+47 918 56 425"] "<br>"
                 [:a {:href "mailto:magnar@kodemaker.no"}
                  "magnar@kodemaker.no"]]]))

(fact (->> (page :recommendations [{:title "Anbefaling 1"
                                    :blurb "Denne er **bra**."
                                    :link {:url "http://example.com" :text "Les detta"}
                                    :tech [{:name "Clojure", :url "/clojure/"}]}])
           :body html remove-flexmark-quirky-newlines)

      => (html [:h2.mhn "Magnars anbefalinger"]
               [:h3 [:a {:href "http://example.com"} "Anbefaling 1"]]
               [:p.near.cookie-w [:span.cookie [:a {:href "/clojure/"} "Clojure"]]]
               [:p "Denne er <strong>bra</strong>. "
                [:a.nowrap {:href "http://example.com"} "Les detta"]]))

(fact (->> (page :hobbies [{:title "Brettspill"
                            :description "Det er mer enn Monopol og Ludo i verden."
                            :illustration "/photos/hobbies/brettspill.jpg"}
                           {:title "Adventur"
                            :description "Hjemmesnekra spill."
                            :url "http://www.adventur.no"
                            :illustration "/photos/hobbies/adventur.jpg"}])
           :body html remove-flexmark-quirky-newlines)

      => (html [:h2.mhn "Snakker gjerne om"]
               [:div.bd
                [:h3.mtn "Brettspill"]
                [:p
                 [:img.illu {:src "/photos/hobbies/brettspill.jpg"}]
                 "Det er mer enn Monopol og Ludo i verden."]]
               [:div.bd
                [:h3.mtn "Adventur"]
                [:p
                 [:a.illu {:href "http://www.adventur.no"}
                  [:img {:src "/photos/hobbies/adventur.jpg"}]]
                 "Hjemmesnekra spill."]]))

(fact (->> (page :tech {:favorites-at-the-moment [{:name "clojure"}
                                                  {:name "emacs"}
                                                  {:name "ansible"}]
                        :want-to-learn-more [{:name "React", :url "/react/"}]})
           :body html)

      => (html [:p
                [:strong "Favoritter for tiden: "]
                "clojure, emacs og ansible"
                "<br>"
                [:strong "Vil lære mer: "]
                [:a {:href "/react/"} "React"]
                "<br>"]))

(fact (->> (page :presentations [{:title "Lyntale: Wrap Ajax'en din"
                                  :blurb "Jeg tegner og forteller."
                                  :tech [{:name "JavaScript", :url "/javascript/"}]
                                  :urls {:video "http://vimeo.com/28764670"
                                         :source "https://github.com/magnars/server-facade"}}])
           :body html)

      => (html [:h2.mhn "Magnars foredrag"]
               [:h3.mtn [:a {:href "http://vimeo.com/28764670"} "Lyntale: Wrap Ajax'en din"]]
               [:p.near.cookie-w [:span.cookie [:a {:href "/javascript/"} "JavaScript"]]]
               [:p "Jeg tegner og forteller. "
                [:a.nowrap {:href "http://vimeo.com/28764670"} "Se video"] " "
                [:a.nowrap {:href "https://github.com/magnars/server-facade"} "Se koden"]]))

(fact (->> (page :presence {:twitter "magnars"})
           :aside last last html)

      => (html [:div.mod
                [:a.presence.twitter {:href "https://twitter.com/magnars/"
                                      :title "Twitter"}]]))

(fact (->> (page :presence {:cv "magnar"})
           :aside last last html)

      => (html [:div.mod
                [:a.presence.cv {:href "/cv/magnar/"
                                 :title "Cv"}]]))

(defn upcoming [title date]
  {:title title
   :date date
   :url "http://vg.no"
   :tech [:javascript]
   :location {:title "I stua" :url "http://127.0.0.1"}
   :description "Something"})

(let [events [(upcoming "Februarkurs" (time/local-date 2013 2 7))
              (upcoming "Aprilkurs" (time/local-date 2013 4 1))]]

  (fact "Includes upcoming events 12 weeks from render date"
        (with-redefs [time/today (constantly (time/local-date 2013 1 1))]
          (let [body (->> (page :upcoming events) :body html)]
            body => #(.contains % "Magnars kommende foredrag/kurs")
            body => #(.contains % "Februarkurs")
            body => #(not (.contains % "Aprilkurs"))))

        (with-redefs [time/today (constantly (time/local-date 2013 2 1))]
          (let [body (->> (page :upcoming events) :body html)]
            body => #(.contains % "Februarkurs")
            body => #(.contains % "Aprilkurs")))))
