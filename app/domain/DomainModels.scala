package domain

import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by prasenjit.b on 10/6/2017.
  */
object DomainModels {


  /*trait MapWsResponseToCustomResponse[Req, Result] {
    def apply( response : WSResponse)( t : Req) : Result
  }
  object MapWsResponseToCustomResponse {
    implicit object MapWsResponseToCustomResponseImpl extends MapWsResponseToCustomResponse[IsUpRequest,IsUpResult] {
      override def apply(wSResponse: WSResponse)(isUpRequest: IsUpRequest): IsUpResult = {
        wSResponse.status match {
          case 200 => IsUpResult( isUpRequest.url, true )
          case _ => IsUpResult( isUpRequest.url, false )
        }
      }
    }
  }*/

  /*trait ErrorHandler[Req, Res] extends ( Req => Future[Res] )
  object ErrorHandler{
    implicit class ErrorHandlerImpl( delegate : IsUpRequest => Future[IsUpResult] )( implicit executor: ExecutionContext ) extends ErrorHandler[ IsUpRequest, IsUpResult] {
      override def apply( isUpRequest: IsUpRequest ): Future[IsUpResult] = {
        delegate( isUpRequest ).recoverWith{ case _ : Throwable => Future( IsUpResult( isUpRequest.url, false ) ) }
      }
    }
  }
  */




  case class IsUpRequest( url : String )

  case class IsUpResult( url: String, up : Boolean )

  trait ServiceRequest[CusReq, WsReq]{
    def apply( ws : WSClient )( t : CusReq) :  WsReq
  }
  object ServiceRequest {
    implicit object ServiceRequestImpl extends ServiceRequest[IsUpRequest, WSRequest] {
      override def apply(ws: WSClient)( isUpRequest: IsUpRequest): WSRequest = ws.url( isUpRequest.url )
    }
  }

  trait MapWsResponseToCustomResponse[Res, Req, Result] extends ( (Res,Req) => Result )
  object MapWsResponseToCustomResponse {
    implicit object MapWsResponseToCustomResponseImpl extends MapWsResponseToCustomResponse[ WSResponse, IsUpRequest, IsUpResult ] {
      override def apply( wSResponse: WSResponse, isUpRequest : IsUpRequest ): IsUpResult = {
        wSResponse.status match {
          case 200 => IsUpResult( isUpRequest.url, true )
          case _ => IsUpResult( isUpRequest.url, false )
        }
      }
    }
  }

  class ErrorHandler( delegate : IsUpRequest => Future[IsUpResult] ) ( implicit executor: ExecutionContext ) extends ( IsUpRequest => Future[IsUpResult] ) {
    override def apply( isUpRequest: IsUpRequest): Future[IsUpResult] = {
      delegate( isUpRequest ).recover{ case e : Throwable => IsUpResult( isUpRequest.url, false ) }
    }
  }

  class IsUpService( ws : WSClient )( implicit ec : ExecutionContext,
                                      serviceRequest : ServiceRequest[IsUpRequest, WSRequest],
                                      mapWsResponseToCustomResponse : MapWsResponseToCustomResponse[WSResponse, IsUpRequest, IsUpResult] )
                                    extends ( IsUpRequest => Future[IsUpResult] ) {
    override def apply( req: IsUpRequest ): Future[IsUpResult] = {
      val serviceRequestVal = serviceRequest( ws ) _
      serviceRequestVal( req ).execute().map( mapWsResponseToCustomResponse( _, req ) )
    }
  }

}
