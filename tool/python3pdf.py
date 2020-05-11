# import sys
import importlib
# importlib.reload(sys)

from pdfminer.pdfparser import PDFParser, PDFDocument
from pdfminer.pdfinterp import PDFResourceManager, PDFPageInterpreter
from pdfminer.converter import PDFPageAggregator
from pdfminer.layout import LTTextBoxHorizontal, LAParams
from pdfminer.pdfinterp import PDFTextExtractionNotAllowed

def readPDF(path, toPath):
    # 以二进制形式打开pdf文件
    with open(path, "rb") as f:
        # 创建一个pdf文档分析器
        parser = PDFParser(f)
        # 创建pdf文档
        pdfFile = PDFDocument()
        # 链接分析器与文档对象
        parser.set_document(pdfFile)
        pdfFile.set_parser(parser)
        # 提供初始化密码
        pdfFile.initialize()
        # 检测文档是否提供txt转换
    if not pdfFile.is_extractable:
        raise PDFTextExtractionNotAllowed
    else:
        # 解析数据
        # 数据管理
        manager = PDFResourceManager()
        # 创建一个PDF设备对象
        laparams = LAParams()
        device = PDFPageAggregator(manager, laparams=laparams)
        # 解释器对象
        interpreter = PDFPageInterpreter(manager, device)

        # 开始循环处理，每次处理一页
        pages = pdfFile.get_pages()
        for page in pages:
            interpreter.process_page(page)
            layout = device.get_result()
            for x in layout:
                if(isinstance(x, LTTextBoxHorizontal)):
                    with open(toPath, "a",encoding='utf-8') as f:
                        str = x.get_text()
                        print(str)
                        f.write(str+"\n")
if __name__ == "__main__":
    path = r"F:/workspace/xiongzhenggang.github.io/AI/data/andrew_ml_ex67101/ex6.pdf"
    toPath = r"F:/book/tmp/a.txt"
    readPDF(path, toPath)
# from urllib.request import urlopen
# from pdfminer.pdfinterp import PDFResourceManager, process_pdf
# from pdfminer.converter import TextConverter
# from pdfminer.layout import LAParams
# from io import StringIO
# from io import open

# def readPDF(pdfFile):
#     rsrcmgr = PDFResourceManager()
#     retstr = StringIO()
#     laparams = LAParams()
#     device = TextConverter(rsrcmgr, retstr, laparams=laparams)

#     process_pdf(rsrcmgr, device, pdfFile)
#     device.close()

#     content = retstr.getvalue()
#     retstr.close()
#     return content

# # pdfFile = urlopen("http://pythonscraping.com/pages/warandpeace/chapter1.pdf")
# pdfFile =  open('F:\\book\\tmp\\1.pdf', "rb")
# outputString = readPDF(pdfFile)
# print(outputString)
# pdfFile.close()