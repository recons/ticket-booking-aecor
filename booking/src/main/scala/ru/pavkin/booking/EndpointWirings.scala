package ru.pavkin.booking

import cats.effect.{ConcurrentEffect, Timer}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import ru.pavkin.booking.booking.endpoint.{BookingRoutes, DefaultBookingEndpoint}
import ru.pavkin.booking.config.HttpServer

import scala.concurrent.duration.{Duration => _}

final class EndpointWirings[F[_] : ConcurrentEffect : Timer](
  httpServer: HttpServer,
  postgresWirings: PostgresWirings[F],
  entityWirings: EntityWirings[F]) {

  import entityWirings._
  import postgresWirings._

  val bookingsEndpoint = new DefaultBookingEndpoint(bookings, bookingViewRepo)

  val bookingRoutes = new BookingRoutes(bookingsEndpoint)

  val routes: HttpRoutes[F] = bookingRoutes.routes

  def launchHttpService: F[Unit] =
    BlazeServerBuilder[F]
      .bindHttp(httpServer.port, httpServer.interface)
      .withHttpApp(Router("/" -> routes).orNotFound)
      .serve
      .compile
      .drain

}
