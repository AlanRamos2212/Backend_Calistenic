from zipfile import ZipFile

from docx import Document

path = r"C:\Proyectos Alan\Backend_Calistenic\CalistenicTrack_Propuesta_Fase2_UTEQ_ACTUALIZADA.docx"
doc = Document(path)
with ZipFile(path) as archive:
    names = archive.namelist()
print("paragraphs", len(doc.paragraphs))
print("tables", len(doc.tables))
print("has_update", any(p.text == "Actualización de avances" for p in doc.paragraphs))
print("last", doc.paragraphs[-1].text)
print("document_xml", "word/document.xml" in names)
print("media", len([name for name in names if name.startswith("word/media/")]))
