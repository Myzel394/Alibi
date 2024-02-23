# ui

This folder contains all user interfaces. The folder is structured as follows:

* `components`: contains all reusable components
    * `atoms`, `molecules`, `organisms`, `pages`: contains components that are generic and can be
      reused in different contexts
    * `<name>Screen/{atoms,molecules,organisms,pages}`: contains components that are specific to a
      screen
* `screens`: contains all screens. Screens are composed of components from the `components` folder
* `models`: contains view models used by the screens
* `utils`: contains general utility functions

The root Kotlin files are used for the general setup of the UI.