DEPRECATED in favor of https://github.com/vineetjohn/questrade-scripts

# Clojure - Questrade

[![Build Status](https://travis-ci.com/vineetjohn/clojure-questrade.svg?token=9zWLe6zMeUk6X9pWXLzU&branch=master)](https://travis-ci.com/vineetjohn/clojure-questrade)

A simple Clojure application designed to retrieve and report account details on Questrade, to help calculate capital gains for tax reporting, especially for non-registered accounts.

## Usage

### Setup
- Install [Clojure](https://clojure.org/)
- Install [Leiningen](https://leiningen.org/)
- [Setup a Questrade application](https://login.questrade.com/APIAccess/UserApps.aspx) and store the refresh token in the [`.auth-tokens`](https://github.com/vineetjohn/clojure-questrade/blob/master/.auth-tokens.json) file.
- Update the [`.accounts.json`](https://github.com/vineetjohn/clojure-questrade/blob/master/.accounts.json) file to reflect your own Questrade accounts.

### Calculate capital gains
- `lein run -a ${ACCOUNT_IDENTIFIER} -y ${TAX_YEAR}`
  - e.g. `lein run -a margin -y 2018`
  - e.g. `lein run -a rrsp -y 2018`

### Run tests
- `lein test`


## License

Copyright © 2019 Vineet John

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
