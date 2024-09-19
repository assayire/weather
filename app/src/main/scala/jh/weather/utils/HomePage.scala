package jh.weather.utils

import laika.api.*
import laika.format.*
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import scala.jdk.CollectionConverters.*

/**
 * A helper to prepare the content for the home page.
 * The home page renders the development notes for the
 * weather app exercise, which is available to read
 * as source (markdown) at resources/notes.md.
 */
object HomePage:
  private final val InsertNotesMd    = "<!-- insert notes.md -->"
  private final val FootnotesSection = """<section class="footnotes" data-footnotes>"""

  private val transformer =
    Transformer
      .from(Markdown)
      .to(HTML)
      .using(GitHubFlavor, SyntaxHighlighting)
      .build

  /**
   * Loading the resource(s) every time so that
   * I can keep writing my notes and refresh the
   * browser to see it renders okay. Yes, this
   * fancy notes on the home page is totally
   * unnecessary . But it was just like 5 minutes
   * of work :-) to read notes without having
   * to open the project source.
   */
  def apply(): String =
    val html      = resourceAsString("home.html")
    val notesMd   = resourceAsString("notes.md")
    val notesHtml = convertMarkdownToHtml(notesMd)
    html
      .replace(InsertNotesMd, notesHtml)
      .replace(FootnotesSection, FootnotesSection + "<h2>Footnotes</h2>")

  private def convertMarkdownToHtml(markdown: String): String = {
    val extensions = List(FootnotesExtension.create()).asJava

    val document =
      Parser
        .builder()
        .extensions(extensions)
        .build()
        .parse(markdown)

    HtmlRenderer
      .builder()
      .extensions(extensions)
      .build()
      .render(document)
  }

  private def laika(): String =
    val html  = resourceAsString("home.html")
    val notes = resourceAsString("notes.md")
    transformer.transform(notes) match
      case Right(notes) => html.replace(InsertNotesMd, notes)
      case Left(ex)     => ex.printStackTrace(System.err); html
