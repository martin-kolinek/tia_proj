package views

import scala.language.implicitConversions
import scala.xml.NodeSeq
import play.api.templates.Html

object Helpers {
	implicit class NodeSeqHtml(n:NodeSeq) {
		def html = Html(n.toString)
	}
	
	implicit class HtmlStringContext(s:StringContext) {
		def html(args:Any*) = Html(s.s(args))
	}
}