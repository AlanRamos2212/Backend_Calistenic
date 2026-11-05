from docx import Document

path = r"C:\Users\USUARIO 1\Documents\CalistenicDocu\CalistenicTrack_Propuesta_Fase2_UTEQ_FINAL.docx"
doc = Document(path)
print("PARAGRAPHS", len(doc.paragraphs), "TABLES", len(doc.tables), "SECTIONS", len(doc.sections))
for index, paragraph in enumerate(doc.paragraphs):
    if paragraph.text.strip():
        print(index, repr(paragraph.text), paragraph.style.name)
for index, table in enumerate(doc.tables):
    print("TABLE", index, len(table.rows), len(table.columns))
    for row in table.rows[:5]:
        print([cell.text for cell in row.cells])
