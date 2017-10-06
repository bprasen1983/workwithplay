package controllers

import javax.inject.Inject

import domain.DomainModels.{ErrorHandler, IsUpRequest, IsUpService}
import play.api.libs.ws.WSClient
import javax.inject._

import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * Created by prasenjit.b on 10/6/2017.
  */
@Singleton
class IsUpController @Inject()( wsClient : WSClient, val controllerComponents: ControllerComponents )
                              (implicit ec : ExecutionContext) extends BaseController {
  val isItUpService = new IsUpService( wsClient )
  val errorHandler = new ErrorHandler( isItUpService )

  def isGoogleUp = Action.async { implicit request : Request[AnyContent] =>
     errorHandler( IsUpRequest("http://www.google.com") ).map{ isUpResponse =>
       if( isUpResponse.up ) Ok("Google is Up") else BadGateway("Google is Down")
     }
  }
}
