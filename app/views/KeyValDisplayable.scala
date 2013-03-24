package views

import play.api.templates.Html

trait KeyValDisplayable[T] {
	def header:Seq[Html]
	def row(t:T):Seq[Html]
}

object KeyValUtils {
	def grid[T:KeyValDisplayable](s:Seq[T]) = {
		val disp = implicitly[KeyValDisplayable[T]]
		html.grid(disp.header, s.map(disp.row(_)))
	}
	
	def details[T:KeyValDisplayable](t:T) = {
		val disp = implicitly[KeyValDisplayable[T]]
		html.details(disp.header, disp.row(t))
	}
}