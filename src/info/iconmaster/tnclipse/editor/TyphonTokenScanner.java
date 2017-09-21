package info.iconmaster.tnclipse.editor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import info.iconmaster.tnclipse.TnClipse;
import info.iconmaster.typhon.antlr.TyphonLexer;
import info.iconmaster.typhon.antlr.TyphonParser;
import info.iconmaster.typhon.antlr.TyphonParser.PackageNameContext;

public class TyphonTokenScanner implements ITokenScanner {
	TyphonLexer lexer;
	String input;

	int lastOffset, lastLength;
	boolean annotMode;
	
	@Override
	public void setRange(IDocument document, int offset, int length) {
		try {
			input = document.get(offset, length);
			lexer = new TyphonLexer(new ANTLRInputStream(input));
			lastOffset = offset;
			lastLength = 0;
		} catch (BadLocationException e) {
			Logger.getGlobal().log(Level.SEVERE, "setRange failed", e);
		}
	}
	
	@Override
	public IToken nextToken() {
		org.antlr.v4.runtime.Token lastToken = lexer.nextToken();
		lastOffset += lastLength;
		lastLength = lastToken.getStopIndex() - lastToken.getStartIndex() + 1;
		
		if (lastToken.getType() == lastToken.EOF) {
			annotMode = false;
			return org.eclipse.jface.text.rules.Token.EOF;
		} else if (lastToken.getType() == TyphonLexer.WHITESPACE) {
			annotMode = false;
			return org.eclipse.jface.text.rules.Token.WHITESPACE;
		} else {
			Color color;
			int style = SWT.NONE;
			
			switch (lastToken.getType()) {
			case TyphonLexer.COMMENT:
			case TyphonLexer.BLOCK_COMMENT:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.comment");
				break;
			case TyphonLexer.CHAR:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.char");
				break;
			case TyphonLexer.DOC_COMMENT:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.doc_comment");
				break;
			case TyphonLexer.NUMBER:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.number");
				break;
			case TyphonLexer.STRING:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.string");
				break;
			case TyphonLexer.UNKNOWN_TOKEN:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.default");
				break;
			case TyphonLexer.WORD:
				if (annotMode) {
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.annotation");
				} else {
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.default");
				}
				break;
			default:
				if (lastToken.getText().equals("@") || lastToken.getText().equals("@@")) {
					// it's an annotation; this color should try to go to the end of the annot or to the paren (best effort)
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.annotation");
					
					annotMode = true;
				} else if (lastToken.getText().equals("(")) {
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.default");
					
					annotMode = false;
				} else if (Character.isAlphabetic(lastToken.getText().charAt(0))) {
					// if it starts with a letter, it's a keyword
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.keyword");
					style = SWT.BOLD;
				} else {
					// else, it's a normal symbol
					if (annotMode && lastToken.getText().equals(".")) {
						color = TnClipse.colorManager.getColorFromPreferences("editor.color.annotation");
					} else {
						color = TnClipse.colorManager.getColorFromPreferences("editor.color.default");
					}
				}
			}
			
			return new org.eclipse.jface.text.rules.Token(new TextAttribute(color, null, style));
		}
	}
	
	@Override
	public int getTokenOffset() {
		return lastOffset;
	}
	
	@Override
	public int getTokenLength() {
		return lastLength;
	}
}
