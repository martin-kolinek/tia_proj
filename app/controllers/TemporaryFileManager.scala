package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.collection.mutable.HashMap
import play.api.Play.current
import play.api.libs.Files.TemporaryFile
import resource._
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream

object TemporaryFileStorage {
	private val hm = new HashMap[String, TemporaryFile]
	private var id = 1
	private def nextId = {
		id += 1
		"%010d".format(id)
	} 
	def addFile(file:TemporaryFile) = synchronized {
		val ref = nextId
		hm(ref) = file
		ref
	}
	
	def deleteFile(ref:String) = synchronized {
		hm(ref).clean
		hm.remove(ref)
	}
	
	def retrieveFile(ref:String) = synchronized {
		val res = for{
			file <- hm.get(ref)
			manag = managed(scala.io.Source.fromFile(file.file))
		} yield manag.acquireAndGet(_.map(_.toByte).toArray)
		deleteFile(ref)
		res
	}
	
	def hasFile(ref:String) = synchronized {
		hm.isDefinedAt(ref)
	}
	
	def createFile(data:Array[Byte]) = {
		val tf = TemporaryFile()
		for(wr <- managed(new FileOutputStream(tf.file))) {
			wr.write(data)
		}
		synchronized {
			val res = nextId
			hm(res)=tf
			res
		}
	}
}

object TemporaryFileManager extends Controller {	
	def upload = Action(parse.temporaryFile){ request =>
		Ok(TemporaryFileStorage.addFile(request.body))
	}
	
	val tempFileMapping = nonEmptyText.
			transform(TemporaryFileStorage.retrieveFile, {
				case None => ""
				case Some(data) => TemporaryFileStorage.createFile(data)
			}:Option[Array[Byte]] => String).
			verifying("non existent file reference", _.isDefined).
			transform(_.get, (x:Array[Byte]) => Some(x))
}