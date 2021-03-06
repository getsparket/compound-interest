(ns flierplath.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [flierplath.events]
            [clojure.data :as d]
            [flierplath.subs]))
(js* "/* @flow */")
(enable-console-print!)
(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def input (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def react-navigation (js/require "react-navigation"))
(def add-navigation-helpers (.-addNavigationHelpers react-navigation))
(def stack-navigator (.-StackNavigator react-navigation))
(def tab-navigator (.-TabNavigator react-navigation))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(def style
  {
   :title       {:font-size   16
                 :font-weight "100"
                 :margin      20
                 :text-align  "center"}
   :button      {:background-color "#999"
                 :padding          10
                 :margin-bottom    20
                 :border-radius    5}
   :button-text {:color       "white"
                 :text-align  "center"
                 :font-weight "bold"}
   :page  {:align-items      "center"
           :justify-content  "center"
           :flex             1
           :background-color "#444444"}})

(def textinput-props {:style {:padding-left 10
                              :font-size 16
                              :border-width 2
                              :border-color "rgba(0,0,0,0.4)"
                              :border-radius 6}
                      :height 40
                      :auto-correct true
                      :maxLength 32
                      :clear-button-mode "always"
                      :returnKeyType "go"
                      })

(defn default-fi [props]
  (let [something (subscribe [:get-time-to-default-fi])]
    [view {:style (:page style)}
     [view {:style {:background-color "rgba(256,256,256,0.5)"
                    :margin-bottom    20}}
      [text {:style (style :title)} "you'll have a miliion dollars on this day" @something]]
     [touchable-highlight {:on-press #(dispatch [:nav/reset "Index"])
                           :style    (style :button)}
      [text {:style (style :button-text)} "back to index"]]]))

(defn db-state [props]
  (let [something (subscribe [:get-db-state])]
    [view {:style (:page style)}
     [view {:style {:background-color "rgba(256,256,256,0.5)"
                    :margin-bottom    20}}
      [text {:style (style :title)} "db state: " @something]]
     [touchable-highlight {:on-press #(dispatch [:nav/reset "Index"])
                           :style    (style :button)}
      [text {:style (style :button-text)} "back to index"]]]))


(defn resd [props]
  (let [number (-> props (get "params") (get "number"))
        route-name "Index"]
    [view {:style (:page style)}
     [view {:style {:background-color "rgba(256,256,256,0.5)"
                    :margin-bottom    20}}
      [text {:style (style :title)} "Number: " number]]
     [touchable-highlight
      {:style    (style :button)
       :on-press #(dispatch
                    [:nav/navigate
                     [#:nav.route {:key       (keyword (str number))
                                   :routeName :Card
                                   :params    {:number (inc number)}}
                      route-name]])}
      [text {:style (style :button-text)} "Next"]]
     [touchable-highlight {:on-press #(dispatch [:nav/reset route-name])
                           :style    (style :button)}
      [text {:style (style :button-text)} "back to index"]]]))


(def name-of-liab (reagent.ratom/atom ""))
(def price-of-liab (reagent.ratom/atom ""))
(defn liabs [props]
  (let [name (-> props (get "params") (get "name"))
        route-name "Index"]
    [view {:style (:page style)}
     [view {:style {:background-color "rgba(256,256,256,0.5)"
                    :margin-bottom    20}}
      [text {:style (style :title)} "add liab: "  @name-of-liab "\nwith price: " @price-of-liab]]
     [input (assoc textinput-props
                   :on-change-text (fn [value]
                                     (let [_ (println "name is:" value @name-of-liab)])
                                     (reset! name-of-liab value)
                                     (r/flush)))]
     [input (assoc textinput-props
                   :placeholder "price. fails unless you type [0-9]*"
                   :on-change-text (fn [value]
                                     (let [_ (println "price is" value @price-of-liab)])
                                     (reset! price-of-liab value)
                                     (r/flush)))]
     [touchable-highlight {:on-press #(dispatch [:nav/reset route-name])
                           :style    (style :button)}
      [text {:style (style :button-text)} "back to index"]]
     [touchable-highlight {:on-press #(dispatch [:add-liab {:fin.stuff/name @name-of-liab :fin.stuff/amount @price-of-liab}])
                           :style    (style :button)}
      [text {:style (style :button-text)} "add to db"]]
     [view [text "liabilities: "  @(subscribe [:list-liabs])]]]))


(def name-of-asset (reagent.ratom/atom ""))
(def price-of-asset (reagent.ratom/atom ""))
(defn assets [props]
  (let [name (-> props (get "params") (get "name"))
        route-name "Index"]
    [view {:style (:page style)}
     [view {:style {:background-color "rgba(256,256,256,0.5)"
                    :margin-bottom    20}}
      [text {:style (style :title)} "add asset: " @name-of-asset "\nwith price: " @price-of-asset]]
     [input (assoc textinput-props
                   :placeholder "name of asset"
                   :on-change-text (fn [value]
                                     (let [_ (println "name is:" value @name-of-asset)])
                                     (reset! name-of-asset value)
                                     (r/flush)))]
     [input (assoc textinput-props
                   :placeholder "price. fails unless you type [0-9]*"
                   :on-change-text (fn [value]
                                     (let [_ (println "price is" value @price-of-asset)])
                                     (reset! price-of-asset value)
                                     (r/flush)))]
     [touchable-highlight {:on-press #(dispatch [:nav/reset route-name])
                           :style    (style :button)}
      [text {:style (style :button-text)} "back to index"]]
     [touchable-highlight {:on-press #(dispatch [:add-asset {:fin.stuff/name @name-of-asset :fin.stuff/amount @price-of-asset}])
                           :style    (style :button)}
      [text {:style (style :button-text)} "add to db"]]
     [view [text "assets:  " @(subscribe [:list-assets])]]]))


(defn settings []
  [view {:style {:flex 1
                 :justify-content "center"
                 :align-items "center"}}
   [text "SETTINGS"]])


(defn app-root [{:keys [navigation]}]
  [view {:style  (:page style)}
   [text {:style (style :title)} "flierplath"]
   [touchable-highlight {:style    (style :button)
                         :on-press #(dispatch
                                      [:nav/navigate
                                       [#:nav.route {:key       :0
                                                     :routeName :Card
                                                     :params    {:number 1}}
                                        "Index"]])}
    [text {:style (style :button-text)} "example navigation"]]
   [touchable-highlight {:style    (style :button)
                         :on-press #(dispatch
                                     [:nav/navigate
                                      [#:nav.route {:key       :0
                                                    :routeName :Settings
                                                    :params    {:number 1}}
                                       "Index"]])}
    [text {:style (style :button-text)} "settings"]]
   [touchable-highlight {:style    (style :button)
                         :on-press #(dispatch
                                     [:nav/navigate
                                      [#:nav.route {:key       :0
                                                    :routeName :Assets
                                                    :params    {:name "m"}}
                                       "Index"]])}
    [text {:style (style :button-text)} "assets"]]
   [touchable-highlight {:style    (style :button)
                         :on-press #(dispatch
                                     [:nav/navigate
                                      [#:nav.route {:key       :0
                                                    :routeName :Liabs
                                                    :params    {:name "m"}}
                                       "Index"]])}
    [text {:style (style :button-text)} "liabs"]]
   [touchable-highlight {:style    (style :button)
                         :on-press #(dispatch
                                     [:nav/navigate
                                      [#:nav.route {:key       :0
                                                    :routeName :DbState
                                                    :params    {:name "m"}}
                                       "Index"]])}
    [text {:style (style :button-text)} "app state"]]
   [touchable-highlight {:style    (style :button)
                         :on-press #(dispatch
                                     [:nav/navigate
                                      [#:nav.route {:key       :0
                                                    :routeName :DefaultFi
                                                    :params    {:name "m"}}
                                       "Index"]])}
    [text {:style (style :button-text)} "default fi"]]])


(defn nav-wrapper [component title]
  (let [comp (r/reactify-component
               (fn [{:keys [navigation]}]
                 [component (-> navigation .-state js->clj)]))]
    (aset comp "navigationOptions" #js {"title" title})
    comp))


(def default-fi-comp (nav-wrapper default-fi #(str "Card "
                                               (aget % "state" "params" "number"))))
(def resd-comp (nav-wrapper resd #(str "Card "
                                       (aget % "state" "params" "number"))))
(def db-state-comp (nav-wrapper db-state #(str "Card "
                                       (aget % "state" "params" "number"))))
(def assets-comp (nav-wrapper assets #(str "Inserting assets screen "
                                       (aget % "state" "params" "number"))))
(def liabs-comp (nav-wrapper liabs #(str "Inserting liabs screen "
                                           (aget % "state" "params" "number"))))
(def settings-comp (nav-wrapper settings #(str "The Settings ")))
(def app-root-comp (nav-wrapper app-root "Welcome"))

(def stack-router {:Home {:screen app-root-comp}
                   :Card {:screen resd-comp}
                   :DefaultFi {:screen default-fi-comp}
                   :DbState {:screen db-state-comp}
                   :Assets {:screen assets-comp}
                   :Liabs {:screen liabs-comp}
                   :Settings {:screen settings-comp}})


(def sn (r/adapt-react-class (stack-navigator (clj->js stack-router))))


(defn card-start [] (let [nav-state (subscribe [:nav/stack-state "Index"])]
                      (fn []
                        (js/console.log @nav-state)
                        [sn {:navigation (add-navigation-helpers
                                           (clj->js
                                             {"dispatch" #(do
                                                            (js/console.log "EVENT" %)
                                                            (dispatch [:nav/js [% "Index"]]))
                                              "state"    (clj->js @nav-state)}))}])))


(def tab-router {:Index    {:screen (nav-wrapper card-start "Index")}
                 :Settings {:screen (nav-wrapper settings "Settings")}})


(defn tab-navigator-inst []
  (tab-navigator
    (clj->js tab-router)
    (clj->js {:order ["Index" "Settings" #_"DbState" #_"Assets"]
              :initialRouteName "Index"})))

(defn get-state [action]
  (-> (tab-navigator-inst)
      .-router
      (.getStateForAction action)))

(defonce tn
  (let [tni (tab-navigator-inst)]
    (aset tni "router" "getStateForAction" #(let [new-state (get-state %)]
                                              (js/console.log "STATE" % new-state)
                                              (dispatch [:nav/set new-state])
                                              new-state) #_(do (js/console.log %)
                                                               #_(get-state %)))
    (r/adapt-react-class tni)))


(defn start []
  (let [nav-state (subscribe [:nav/tab-state])]
    (fn []
      [tn])))


(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "flierplath" #(r/reactify-component start)))
