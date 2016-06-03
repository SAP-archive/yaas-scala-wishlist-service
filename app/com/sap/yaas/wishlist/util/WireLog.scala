/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
package com.sap.yaas.wishlist.util


import javax.inject.Inject

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import org.slf4j.LoggerFactory
import play.api.libs.streams.Accumulator
import play.api.mvc.{EssentialAction, EssentialFilter}
import play.api.{Environment, Mode}

import scala.concurrent.ExecutionContext

class WireLog @Inject()(env: Environment)(implicit mat: Materializer, ec: ExecutionContext) extends EssentialFilter {

  val log = LoggerFactory.getLogger("yass.wishlist.wirelog")

  /*
  The reason why it's not straight forward to log the request body is that the body is a potentially infinite stream.
  Underlying optimisations aside, there is no point at which the entire body exists in memory for you to log it,
  because doing so would require infinite memory.
  That said, usually the body is not infinite in length, and it's quite possible to buffer it in memory
  - in fact this is how most of the body parsers work - though it's important to implement limits on this
  otherwise you open yourself up to a trivial DoS attack.
  In Play 2.5, you can return an Accumulator that buffers the body from a filter,
  after buffering the body, you can log it, and then you can pass it down to the Accumulator
  from the next call in the chain.  Without the infinite buffer protection, it would look like this:
 */

  def apply(next: EssentialAction): EssentialAction =

  /**
    * The wire log can lead to memory overflows and should not be used in production. Hence the mode check.
    */
    if (env.mode != Mode.Prod && log.isInfoEnabled) {
      EssentialAction { req =>
        log.info("=== REQUEST ===")
        req.headers.headers.foreach(header => log.info(header.toString))
        Accumulator[ByteString, ByteString](Sink.fold(ByteString.empty)(_ ++ _)).mapFuture { body =>
          // Now you have the request header in "req" and  the request body in "body", log it:
          log.info("--------------------")
          log.info(body.toString())
          log.info("--------------------")
          // Now invoke the action and feed the buffered body into it
          next(req).run(Source.single(body)).map(
            response => {
              response.header.headers.foreach(header => log.info(header.toString))
              // TODO log response?
              response
            }
          )
        }
      }
    } else {
      next
    }

  /*
  Providing protection against out of memory errors requires sending the stream through a flow that
  will only take a limited number of bytes.  While that's very simple to do with Akka streams naively,
  the problem is you don't want to just drop the remainder, which is what things like Flow.takeWhile
  does, because then the body you get will be incomplete, so if you pass that to the action, it won't
  get the full body, and that's a big problem.  So what you really want to do is buffer up to a certain
  point, and then log once you reach that point, or if you don't, log once the body is complete.
  This is actually really not simple to do in Akka streams - if you're using Play 2.4, it's trivial
  because you can just use iteratees which make this kind of thing easy, but Akka streams requires
  a lot of moving parts to keep track of the state and whether you've logged yet or not and make
  sure you log when the stream completes if you haven't already, etc.
   */
}
