/*
 * Copyright 2011-2019 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.core.check

import java.util.{ Map => JMap }

import io.gatling.commons.validation._
import io.gatling.core.session.{ Expression, Session }

trait TypedConditionalCheckWrapper[R, C <: Check[R]] {
  def wrap(condition: (R, Session) => Validation[Boolean], thenCheck: C): C
}

trait UntypedConditionalCheckWrapper[C <: Check[_]] {
  def wrap(condition: Expression[Boolean], thenCheck: C): C
}

final case class ConditionalCheck[R, C <: Check[R]](condition: (R, Session) => Validation[Boolean], thenCheck: C) extends Check[R] {

  def performNestedCheck(nestedCheck: Check[R], response: R, session: Session)(implicit cache: JMap[Any, Any]): Validation[CheckResult] = {
    nestedCheck.check(response, session)
  }

  def check(response: R, session: Session)(implicit cache: JMap[Any, Any]): Validation[CheckResult] =
    condition(response, session).flatMap { boolean =>
      if (boolean) {
        performNestedCheck(thenCheck, response, session)
      } else {
        CheckResult.NoopCheckResultSuccess
      }
    }
}
