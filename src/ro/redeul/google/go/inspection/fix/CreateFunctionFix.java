package ro.redeul.google.go.inspection.fix;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.redeul.google.go.lang.psi.expressions.literals.GoLiteralFunction;
import ro.redeul.google.go.lang.psi.toplevel.GoFunctionDeclaration;
import ro.redeul.google.go.util.GoUtil;

import static ro.redeul.google.go.lang.psi.utils.GoPsiUtils.findParentOfType;
import static ro.redeul.google.go.util.EditorUtil.pressEnter;
import static ro.redeul.google.go.util.EditorUtil.reformatLines;

public class CreateFunctionFix extends LocalQuickFixAndIntentionActionOnPsiElement {
    public CreateFunctionFix(@Nullable PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return "Create function \"" + getStartElement().getText() + "\"";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Variable Declaration";
    }


    @Override
    public void invoke(@NotNull Project project,
                       @NotNull final PsiFile file,
                       @Nullable("is null when called from inspection") final Editor editor,
                       @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        final PsiElement e = startElement;


        GoFunctionDeclaration fd = findParentOfType(e, GoFunctionDeclaration.class);
        while (fd instanceof GoLiteralFunction) {
            fd = findParentOfType(fd.getParent(), GoFunctionDeclaration.class);
        }

        final String fnArguments = GoUtil.InspectionGenFuncArgs(e);

        final int insertPoint;
        if (fd != null) {
            insertPoint = fd.getTextRange().getEndOffset();
        } else {
            insertPoint = file.getTextRange().getEndOffset();
        }

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                Document doc = PsiDocumentManager.getInstance(e.getProject()).getDocument(file);
                doc.insertString(insertPoint, String.format("\n\nfunc %s(%s) {\n}\n", e.getText(), fnArguments));
                if (editor != null) {
                    int line = doc.getLineNumber(insertPoint);
                    int offset = doc.getLineEndOffset(line + 2);
                    editor.getCaretModel().moveToOffset(offset);
                    reformatLines(file, editor, line, line + 3);
                    pressEnter(editor);
                }
            }
        });
    }
}
