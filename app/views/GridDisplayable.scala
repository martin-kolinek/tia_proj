package views

import play.api.templates.Html

trait GridDisplayable[T] {
	def header:Seq[Html]
	def row(t:T):Seq[Html]
}

object Grid {
	def grid[T:GridDisplayable](s:Seq[T]) = {
		val disp = implicitly[GridDisplayable[T]]
		html.grid(disp.header, s.map(disp.row(_)))
	}
}