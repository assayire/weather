package jh.weather.utils

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.html.HtmlRenderer.*
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension.*
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.format.options.ElementPlacement

import java.util.Arrays as JArrays

object FlexMarkIt:
  def apply(markdown: String): String = {
    // Create a new MutableDataSet for options
    val options = MutableDataSet()
    // Set options to render a fragment instead of a full document
    options.set(RENDER_HEADER_ID, false)
    options.set(GENERATE_HEADER_ID, false)
    options.set(SUPPRESS_HTML, true)
    /*options.set(FOOTNOTE_BACK_LINK_REF, "↩")
    options.set(FOOTNOTE_BACK_LINK_STRING, "<sup>↩</sup>")*/
    options.set(FOOTNOTE_LINK_REF_CLASS, "footnote-ref")
    options.set(FootnoteExtension.FOOTNOTE_BACK_REF_STRING, "↩")
    options.set(FOOTNOTE_PLACEMENT, ElementPlacement.GROUP_WITH_FIRST)
    options.set(Parser.EXTENSIONS, JArrays.asList(FootnoteExtension.create()))

    val document = Parser.builder(options).build.parse(markdown)
    HtmlRenderer.builder(options).build.render(document)
  }
