(ns kodemaker-no.cultivate.people-test
  (:require [kodemaker-no.cultivate.people :refer :all]
            [midje.sweet :refer :all]
            [kodemaker-no.validate :refer [validate-content]]
            [kodemaker-no.cultivate.content-shells :as c]))

(def content
  (c/content
   {:people {:magnar (c/person {:id :magnar
                                :name ["Magnar" "Sveen"]})
             :finnjoh (c/person {:id :finnjoh
                                 :name ["Finn" "J" "Johnsen"]})
             :andersf (c/person {:id :andersf
                                 :name ["Anders" "Furseth"]})}}))

(defn cultivate [content]
  (cultivate-people (validate-content content)))

(let [people (cultivate content)]

  (fact (-> people :magnar :full-name) => "Magnar Sveen"
        (-> people :finnjoh :full-name) => "Finn J Johnsen")

  (fact (-> people :magnar :first-name) => "Magnar"
        (-> people :finnjoh :first-name) => "Finn")

  (fact (-> people :magnar :genitive) => "Magnars"
        (-> people :andersf :genitive) => "Anders'")

  (fact (-> people :magnar :str) => "magnar"
        (-> people :finnjoh :str) => "finnjoh")

  (fact (-> people :magnar :url) => "/magnar/"
        (-> people :finnjoh :url) => "/finnjoh/")

  (fact (-> people :magnar :photos) => {:side-profile "/photos/people/magnar/side-profile.jpg"
                                        :half-figure "/photos/people/magnar/half-figure.jpg"}
        (-> people :finnjoh :photos) => {:side-profile "/photos/people/finnjoh/side-profile.jpg"
                                         :half-figure "/photos/people/finnjoh/half-figure.jpg"}))

(let [people (-> content
                 (assoc-in [:people :magnar :tech]
                           {:favorites-at-the-moment [:clojure]
                            :want-to-learn-more [:react]})
                 (assoc-in [:people :magnar :recommendations]
                           [(c/recommendation {:tech [:ansible]})])
                 (assoc-in [:tech :react]
                           {:id :react
                            :name "React"
                            :site "http://react.js"
                            :description "Blah!"})
                 cultivate)]

  (fact
   "Tech that isn't present in the content is given a name based
    on its :id."
   (-> people :magnar :tech :favorites-at-the-moment)
   => [{:id :clojure, :name "clojure"}]

   (-> people :magnar :recommendations first :tech)
   => [{:id :ansible, :name "ansible"}])

  (fact
   "Tech that is present, uses the :name in the tech, and adds
    a :url based on the :id."
   (-> people :magnar :tech :want-to-learn-more)
   => [{:id :react, :name "React", :url "/react/"}])

  (fact
   "It doesn't add empty maps and lists"

   (-> people :finnjoh :tech) => nil))

(let [people (-> content
                 (assoc-in [:people :magnar :endorsements]
                           [{:project :finn-oppdrag
                             :author "Kaija Ommundsen"
                             :photo "/thumbs/faces/kaija-ommundsen.jpg"
                             :quote "Jeg har hatt glede av å jobbe med Magnar."}
                            {:project :finn-surf-sammen
                             :author "Bjørn Henrik Vangstein"
                             :photo "/thumbs/faces/bjorn-henrik-vangstein.jpg"
                             :quote "Magnar Sveen skiller seg klart ut i mengden."}])
                 (assoc-in [:people :magnar :projects]
                           [{:id :finn-oppdrag
                             :customer "FINN oppdrag"
                             :description ""
                             :tech []}])
                 (assoc-in [:projects :finn-surf-sammen]
                           (c/project {:id :finn-surf-sammen
                                       :name "FINN surf sammen"}))
                 cultivate)]

  (fact "It fetches the project name from the one listed in your
         profile. Only official projects are given URLs."

        (-> people :magnar :endorsements)

        => [{:project {:id :finn-oppdrag, :name "FINN oppdrag"}
             :author "Kaija Ommundsen"
             :photo "/thumbs/faces/kaija-ommundsen.jpg"
             :quote "Jeg har hatt glede av å jobbe med Magnar."}
            {:project {:id :finn-surf-sammen, :name "FINN surf sammen", :url "/finn-surf-sammen/"}
             :author "Bjørn Henrik Vangstein"
             :photo "/thumbs/faces/bjorn-henrik-vangstein.jpg"
             :quote "Magnar Sveen skiller seg klart ut i mengden."}]))
