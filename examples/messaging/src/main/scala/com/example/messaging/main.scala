/*
 * Copyright 2016 Daniel Urban and contributors listed in AUTHORS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.messaging

import fs2.Task

import org.http4s._
import org.http4s.dsl._
import org.http4s.server.ServerApp
import org.http4s.circe._

import io.sigs.seals._
import io.sigs.seals.circe.Codecs._

object Protocol {
  final case class Ping(seqNr: Long, payload: Vector[Int])
  final case class Pong(seqNr: Long)
  final case class PingIncompatible(seqNr: Long, payload: Vector[Int], flags: Int)
}

object MyClient extends App {

  import org.http4s.client.blaze._
  import Protocol._

  val client = PooledHttp1Client()

  val pongGood = jsonEncoderOf[Envelope[Ping]].toEntity(
    Envelope(Ping(42L, Vector(1, 2, 3, 4)))
  ).flatMap(ping).unsafeRunSync()
  assert(pongGood == Pong(42L))
  println(pongGood)

  try {
    val pongBad = jsonEncoderOf[Envelope[PingIncompatible]].toEntity(
      Envelope(PingIncompatible(99L, Vector(4, 5), 0))
    ).flatMap(ping).unsafeRunSync()
    println(pongBad)
  } finally {
    client.shutdownNow()
  }

  def ping(ping: Entity): Task[Pong] = {
    for {
      pong <- client
        .expect(Request(
          POST,
          Uri(authority = Some(Uri.Authority(port = Some(1234))), path = "/test"),
          body = ping.body
        ))(jsonOf[Envelope[Pong]])
    } yield pong.value
  }
}

object MyServer extends ServerApp {

  import org.http4s.server.blaze._
  import Protocol._

  val service = HttpService {
    case p @ POST -> Root / "test" =>
      for {
        env <- p.as(jsonOf[Envelope[Ping]])
        resp <- Ok(Envelope(Pong(env.value.seqNr)))(jsonEncoderOf)
      } yield resp
  }

  override def server(args: List[String]) = {
    BlazeBuilder
      .bindHttp(1234, "localhost")
      .mountService(service, "/")
      .start
  }
}
