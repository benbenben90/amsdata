package com.sip.ams.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.sip.ams.entities.Article;
import com.sip.ams.entities.Provider;
import com.sip.ams.repositories.ArticleRepository;
import com.sip.ams.repositories.ProviderRepository;

@Controller
@RequestMapping("/article/")
public class ArticleController {
	public static String uploadDirectory = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
	private final ArticleRepository articleRepository;
	private final ProviderRepository providerRepository;

	@Autowired
	public ArticleController(ArticleRepository articleRepository, ProviderRepository providerRepository) {
		this.articleRepository = articleRepository;
		this.providerRepository = providerRepository;
	}

	@GetMapping("list")
	public String listProviders(Model model) {
		List<Article> lp = (List<Article>) articleRepository.findAll();
		if (lp.size() == 0)
			lp = null;
		model.addAttribute("articles", lp);
		return "article/listArticles";
	}

	@GetMapping("add")
	public String showAddArticleForm(Article article, Model model) {
		model.addAttribute("providers", providerRepository.findAll());
		model.addAttribute("article", new Article());
		return "article/addArticle";
	}

	@PostMapping("add")
	public String addArticle(@Valid Article article, BindingResult result,
			@RequestParam(name = "providerId", required = false) Long p, @RequestParam("files") MultipartFile[] files) {
		Provider provider = providerRepository.findById(p)
				.orElseThrow(() -> new IllegalArgumentException("Invalid provider Id:" + p));
		article.setProvider(provider);
		/// part upload
		StringBuilder fileName = new StringBuilder();
		MultipartFile file = files[0];
		Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
		fileName.append(file.getOriginalFilename());
		try {
			Files.write(fileNameAndPath, file.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		article.setPicture(fileName.toString());
		articleRepository.save(article);
		return "redirect:list";
	}

	@GetMapping("delete/{id}")
	public String deleteProvider(@PathVariable("id") long id, Model model) {
		Article artice = articleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid providerId:" + id));
		articleRepository.delete(artice);
		model.addAttribute("articles", articleRepository.findAll());
		return "article/listArticles";
	}

	@GetMapping("edit/{id}")
	public String showArticleFormToUpdate(@PathVariable("id") long id, Model model) {
		Article article = articleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid providerId:" + id));
		model.addAttribute("article", article);
		model.addAttribute("providers", providerRepository.findAll());
		model.addAttribute("idProvider", article.getProvider().getId());
		return "article/updateArticle";
	}

	@PostMapping("edit/{id}")
	public String updateArticle(@PathVariable("id") long id, @Valid Article article, BindingResult result, Model model,
			@RequestParam(name = "providerId", required = false) Long p) {
		if (result.hasErrors()) {
			article.setId(id);
			return "article/updateArticle";
		}
		Provider provider = providerRepository.findById(p)
				.orElseThrow(() -> new IllegalArgumentException("Invalid provider Id:" + p));
		article.setProvider(provider);
		articleRepository.save(article);
		model.addAttribute("articles", articleRepository.findAll());
		return "article/listArticles";
	}

	@GetMapping("show/{id}")
	public String showArticleDetails(@PathVariable("id") long id, Model model) {
		Article article = articleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid providerId:" + id));
		model.addAttribute("article", article);
		System.out.println("OK");
		return "article/showArticle";
	}

	@OneToMany(cascade=CascadeType.ALL, mappedBy = "provider")
	private List<Article> articles;
	public List<Article> getArticles() {
	return articles;
	}
	public void setArticles(List<Article> articles) {
	this.articles = articles;
	}
}
